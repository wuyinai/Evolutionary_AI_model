package com.example.evolutionary_ai_model.common.constant;

public final class SecurityConstants {

    private SecurityConstants() {}

    public static final String LOGIN_URL = "/auth/login";
    public static final String REGISTER_URL = "/auth/register";
    //白名单路径
    public static final String[] PERMIT_ALL_URLS = {
            LOGIN_URL,
            REGISTER_URL,
            "/doc.html",
            "/webjars/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/favicon.ico",
            "/hello",
            "/chat/**",  // 对话接口允许匿名访问（测试用）
            "/ai/provider/list"  // 供应商列表允许公开访问（查看供应商信息）
            // 注意：/ai/config/** 和 /ai/provider-config/** 需要用户认证
            // 因为配置管理涉及用户的个人配置信息，需要登录才能访问
    };
    //JWT令牌中存储的声明字段名
    public static final String TOKEN_CLAIM_USERID = "userId";
    public static final String TOKEN_CLAIM_USERNAME = "username";

    //SpringSecurity角色前缀
    private static final String ROLE_PREFIX = "ROLE_";
    public static String getRolePrefix() {
        return ROLE_PREFIX;
    }
}
