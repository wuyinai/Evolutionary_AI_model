package com.example.evolutionary_ai_model.service;

import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.dto.RegisterBody;

/**
 * 用法：认证服务接口，定义登录、注册等认证相关的业务方法
 */
public interface AuthService {

    /**
     * 用户注册
     *
     * @param registerBody 注册请求参数
     * @return 注册结果
     */
    Result<Void> register(RegisterBody registerBody);
}
