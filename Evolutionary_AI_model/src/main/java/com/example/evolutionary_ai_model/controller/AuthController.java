package com.example.evolutionary_ai_model.controller;

import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.entity.dto.RegisterBody;
import com.example.evolutionary_ai_model.security.JwtTokenProvider;
import com.example.evolutionary_ai_model.security.LoginUserDetails;
import com.example.evolutionary_ai_model.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 用法：认证控制器，提供登录、注册等认证相关的REST接口
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    //认证管理器，用于执行SpringSecurity认证流程
    private final AuthenticationManager authenticationManager;
    //JWT令牌工具，用于签发Token
    private final JwtTokenProvider jwtTokenProvider;
    //认证服务，处理注册等业务逻辑
    private final AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody Map<String, String> loginBody) {
        String username = loginBody.get("username");
        String password = loginBody.get("password");

        //调用AuthenticationManager执行认证（用户名+密码校验）
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        //认证通过后获取用户详情
        LoginUserDetails loginUserDetails = (LoginUserDetails) authentication.getPrincipal();

        //签发JWT令牌
        String token = jwtTokenProvider.generateToken(
                loginUserDetails.getUserId(),
                loginUserDetails.getUsername()
        );

        return Result.success("登录成功", Map.of("token", token));
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Validated RegisterBody registerBody) {
        return authService.register(registerBody);
    }
}
