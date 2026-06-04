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
            "/chat/test",
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
