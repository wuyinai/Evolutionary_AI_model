package com.example.evolutionary_ai_model.security;

import com.example.evolutionary_ai_model.entity.SysPermission;
import com.example.evolutionary_ai_model.entity.SysUser;
import com.example.evolutionary_ai_model.mapper.SysPermissionMapper;
import com.example.evolutionary_ai_model.mapper.SysUserMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用处 ：继承 OncePerRequestFilter
 * （保证每次请求只过滤一次），是整个 JWT 认证的入口：
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final SysUserMapper sysUserMapper;
    private final SysPermissionMapper sysPermissionMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        //从请求头中提取令牌
        String token = resolveToken(request);
        //验证令牌有效性
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            //从令牌中取出userId，重新加载用户信息和权限
            Long userId = jwtTokenProvider.getUserIdFromToken(token);

            SysUser sysUser = sysUserMapper.selectById(userId);
            if (sysUser == null) {
                throw new UsernameNotFoundException("用户不存在");
            }

            List<SysPermission> permissions = sysPermissionMapper.selectPermissionsByUserId(userId);
            List<String> permissionCodes = permissions.stream()
                    .map(SysPermission::getPermissionCode)
                    .collect(Collectors.toList());

            LoginUserDetails loginUserDetails = new LoginUserDetails(sysUser, permissionCodes);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(loginUserDetails, null, loginUserDetails.getAuthorities());
            //将认证信息存入SecurityContextHolder,后续的权限校验就能获取到当前用户
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
