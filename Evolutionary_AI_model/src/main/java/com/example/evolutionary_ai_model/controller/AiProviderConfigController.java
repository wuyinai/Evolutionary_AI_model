package com.example.evolutionary_ai_model.controller;

import com.example.evolutionary_ai_model.annotation.OperationLog;
import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.entity.AiProviderConfig;
import com.example.evolutionary_ai_model.entity.vo.AiProviderConfigVO;
import com.example.evolutionary_ai_model.service.AiProviderConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用法：AI供应商配置Controller，负责接收前端供应商配置相关请求。
 * 位于表现层，只负责接收请求、调用业务层、返回响应。
 * 提供供应商连接配置的增删改查功能，管理API密钥、端点地址等连接信息。
 */
@RestController
@RequestMapping("/ai/provider-config")
public class AiProviderConfigController {

    private static final Logger logger = LoggerFactory.getLogger(AiProviderConfigController.class);

    @Autowired
    private AiProviderConfigService providerConfigService;

    /**
     * 获取用户的供应商配置列表
     * 请求地址: GET /ai/provider-config/list
     * 返回数据: 用户的所有供应商配置列表
     */
    @GetMapping("/list")
    public Result<List<AiProviderConfigVO>> list(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("获取供应商配置列表请求");

        try {
            Long userId = getUserId(userDetails);
            List<AiProviderConfigVO> list = providerConfigService.listByUserId(userId);
            logger.info("获取供应商配置列表成功，数量: {}", list.size());
            return Result.success(list);
        } catch (Exception e) {
            logger.error("获取供应商配置列表异常", e);
            return Result.fail("获取供应商配置列表失败");
        }
    }

    /**
     * 添加供应商配置
     * 请求地址: POST /ai/provider-config/add
     * 测试数据示例:
     * {
     *   "configName": "我的OpenAI配置",
     *   "providerCode": "OPENAI",
     *   "protocolType": "OPENAI",
     *   "apiKey": "sk-xxx",
     *   "apiEndpoint": "https://api.openai.com",
     *   "isDefault": 1
     * }
     */
    @PostMapping("/add")
    @OperationLog("添加供应商配置")
    public Result<Long> add(@AuthenticationPrincipal UserDetails userDetails,
                           @RequestBody AiProviderConfig config) {
        logger.info("添加供应商配置请求，供应商编码: {}", config.getProviderCode());

        try {
            Long userId = getUserId(userDetails);
            Long configId = providerConfigService.addConfig(userId, config);
            logger.info("添加供应商配置成功，配置ID: {}", configId);
            return Result.success(configId);
        } catch (IllegalArgumentException e) {
            logger.warn("添加供应商配置参数错误: {}", e.getMessage());
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            logger.error("添加供应商配置异常", e);
            return Result.fail("添加供应商配置失败");
        }
    }

    /**
     * 更新供应商配置
     * 请求地址: PUT /ai/provider-config/update
     */
    @PutMapping("/update")
    public Result<Void> update(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestBody AiProviderConfig config) {
        logger.info("更新供应商配置请求，配置ID: {}", config.getId());

        try {
            Long userId = getUserId(userDetails);
            providerConfigService.updateConfig(userId, config);
            logger.info("更新供应商配置成功");
            return Result.success();
        } catch (IllegalArgumentException e) {
            logger.warn("更新供应商配置参数错误: {}", e.getMessage());
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            logger.error("更新供应商配置异常", e);
            return Result.fail("更新供应商配置失败");
        }
    }

    /**
     * 删除供应商配置
     * 请求地址: DELETE /ai/provider-config/delete/{id}
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@AuthenticationPrincipal UserDetails userDetails,
                              @PathVariable Long id) {
        logger.info("删除供应商配置请求，配置ID: {}", id);

        try {
            Long userId = getUserId(userDetails);
            providerConfigService.deleteConfig(userId, id);
            logger.info("删除供应商配置成功");
            return Result.success();
        } catch (IllegalArgumentException e) {
            logger.warn("删除供应商配置参数错误: {}", e.getMessage());
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            logger.error("删除供应商配置异常", e);
            return Result.fail("删除供应商配置失败");
        }
    }

    /**
     * 设置默认供应商配置
     * 请求地址: PUT /ai/provider-config/set-default/{id}
     */
    @PutMapping("/set-default/{id}")
    @OperationLog("设置默认供应商配置")
    public Result<Void> setDefault(@AuthenticationPrincipal UserDetails userDetails,
                                   @PathVariable Long id) {
        logger.info("设置默认供应商配置请求，配置ID: {}", id);

        try {
            Long userId = getUserId(userDetails);
            providerConfigService.setDefault(userId, id);
            logger.info("设置默认供应商配置成功");
            return Result.success();
        } catch (IllegalArgumentException e) {
            logger.warn("设置默认供应商配置参数错误: {}", e.getMessage());
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            logger.error("设置默认供应商配置异常", e);
            return Result.fail("设置默认供应商配置失败");
        }
    }

    /**
     * 测试供应商连接
     * 请求地址: POST /ai/provider-config/test/{id}
     */
    @PostMapping("/test/{id}")
    @OperationLog("测试供应商连接")
    public Result<String> test(@PathVariable Long id) {
        logger.info("测试供应商连接请求，配置ID: {}", id);

        try {
            String result = providerConfigService.testConnection(id);
            logger.info("测试供应商连接成功，结果: {}", result);
            return Result.success(result);
        } catch (Exception e) {
            logger.error("测试供应商连接异常", e);
            return Result.fail("测试连接失败: " + e.getMessage());
        }
    }

    /**
     * 从认证信息获取用户ID
     */
    private Long getUserId(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("用户未登录");
        }
        // 这里需要根据实际的UserDetails实现获取用户ID
        // 假设LoginUserDetails中包含用户ID
        if (userDetails instanceof com.example.evolutionary_ai_model.security.LoginUserDetails) {
            return ((com.example.evolutionary_ai_model.security.LoginUserDetails) userDetails).getUserId();
        }
        throw new IllegalArgumentException("无法获取用户ID");
    }
}