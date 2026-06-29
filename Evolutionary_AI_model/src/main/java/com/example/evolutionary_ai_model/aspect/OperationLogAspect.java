package com.example.evolutionary_ai_model.aspect;

import com.example.evolutionary_ai_model.annotation.OperationLog;
import com.example.evolutionary_ai_model.entity.SysOperationLog;
import com.example.evolutionary_ai_model.service.SysOperationLogService;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用法：操作日志切面，拦截 @OperationLog 注解的方法，自动采集请求信息并保存操作日志。
 * 通过 AOP Around 通知在方法执行前后记录所需信息，支持异步持久化。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final SysOperationLogService sysOperationLogService;

    /**
     * 环绕通知，拦截所有标注 @OperationLog 的方法
     */
    @Around("@annotation(com.example.evolutionary_ai_model.annotation.OperationLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 执行原方法
        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            // 记录失败日志
            recordLog(joinPoint, startTime, null, e);
            throw e;
        }

        // 记录成功日志
        recordLog(joinPoint, startTime, result, null);
        return result;
    }

    /**
     * 记录操作日志
     */
    private void recordLog(ProceedingJoinPoint joinPoint, long startTime,
                           Object result, Throwable error) {
        try {
            // 获取注解
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            OperationLog annotation = method.getAnnotation(OperationLog.class);
            if (annotation == null) {
                return;
            }

            // 获取HTTP请求信息
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }
            HttpServletRequest request = attributes.getRequest();

            // 获取当前用户
            String username = "";
            Long userId = null;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof com.example.evolutionary_ai_model.security.LoginUserDetails) {
                    com.example.evolutionary_ai_model.security.LoginUserDetails userDetails =
                            (com.example.evolutionary_ai_model.security.LoginUserDetails) principal;
                    userId = userDetails.getUserId();
                    username = userDetails.getUsername();
                } else {
                    username = authentication.getName();
                }
            }

            // 解析 User-Agent
            String uaString = request.getHeader("User-Agent");
            String browser = "";
            String os = "";
            if (uaString != null) {
                UserAgent userAgent = UserAgentUtil.parse(uaString);
                if (userAgent != null) {
                    browser = userAgent.getBrowser() != null
                            ? userAgent.getBrowser().getName() : "";
                    os = userAgent.getOs() != null
                            ? userAgent.getOs().getName() : "";
                }
            }

            // 获取请求参数（排除敏感字段）
            String params = getParams(joinPoint, annotation.excludeParams());

            // 构建日志实体
            SysOperationLog operationLog = new SysOperationLog();
            operationLog.setUserId(userId);
            operationLog.setUsername(username);
            operationLog.setOperation(annotation.value());
            operationLog.setMethod(
                    joinPoint.getTarget().getClass().getName() + "." + method.getName());
            operationLog.setRequestMethod(request.getMethod());
            operationLog.setRequestUrl(request.getRequestURI());
            operationLog.setRequestParams(params);
            operationLog.setRequestTime(System.currentTimeMillis() - startTime);
            operationLog.setIp(getIpAddress(request));
            operationLog.setLocation(""); // 默认空，可根据IP查询地理位置
            operationLog.setBrowser(browser);
            operationLog.setOs(os);
            operationLog.setStatus(error == null ? 1 : 0);
            if (error != null) {
                operationLog.setErrorMsg(truncate(error.getMessage(), 500));
            }

            // 异步保存
            sysOperationLogService.saveLog(operationLog);

        } catch (Exception e) {
            log.warn("记录操作日志异常: {}", e.getMessage());
        }
    }

    /**
     * 获取请求参数（过滤敏感字段）
     */
    private String getParams(ProceedingJoinPoint joinPoint, String[] excludeParams) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] paramValues = joinPoint.getArgs();

            if (paramNames == null || paramValues == null) {
                return "";
            }

            Map<String, Object> paramsMap = new HashMap<>();
            for (int i = 0; i < paramNames.length; i++) {
                // 跳过 HttpServletRequest、HttpServletResponse 等容器对象
                if (paramValues[i] instanceof jakarta.servlet.ServletRequest
                        || paramValues[i] instanceof jakarta.servlet.ServletResponse
                        || paramValues[i] instanceof org.springframework.web.multipart.MultipartFile) {
                    continue;
                }
                // 过滤敏感字段
                boolean isSensitive = isExcluded(paramNames[i], excludeParams);
                paramsMap.put(paramNames[i], isSensitive ? "******" : paramValues[i]);
            }

            if (paramsMap.isEmpty()) {
                return "";
            }
            return paramsMap.entrySet().stream()
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining("&"));
        } catch (Exception e) {
            log.warn("获取请求参数异常: {}", e.getMessage());
            return "";
        }
    }

    /**
     * 判断参数名是否在排除列表中
     */
    private boolean isExcluded(String paramName, String[] excludeParams) {
        if (excludeParams == null) {
            return false;
        }
        String lowerName = paramName.toLowerCase();
        for (String exclude : excludeParams) {
            if (lowerName.contains(exclude.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取客户端IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理的情况，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 截断字符串到指定长度
     */
    private String truncate(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        return str.length() <= maxLength ? str : str.substring(0, maxLength);
    }
}
