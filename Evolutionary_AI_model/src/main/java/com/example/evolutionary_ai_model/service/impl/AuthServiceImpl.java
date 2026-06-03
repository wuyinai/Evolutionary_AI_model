package com.example.evolutionary_ai_model.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.dto.RegisterBody;
import com.example.evolutionary_ai_model.entity.SysUser;
import com.example.evolutionary_ai_model.entity.SysUserRole;
import com.example.evolutionary_ai_model.mapper.SysUserRoleMapper;
import com.example.evolutionary_ai_model.mapper.SysUserMapper;
import com.example.evolutionary_ai_model.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用法：认证服务实现类，处理用户注册等认证业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    //密码加密器，用于BCrypt加密
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> register(RegisterBody registerBody) {
        //校验两次密码是否一致
        if (!registerBody.getPassword().equals(registerBody.getConfirmPassword())) {
            return Result.fail("两次输入的密码不一致");
        }

        //校验用户名是否已存在
        SysUser existUser = sysUserMapper.selectByUsername(registerBody.getUsername());
        if (existUser != null) {
            return Result.fail("用户名已存在");
        }

        //构建用户实体并加密密码
        SysUser sysUser = new SysUser();
        sysUser.setUsername(registerBody.getUsername());
        sysUser.setPassword(passwordEncoder.encode(registerBody.getPassword()));
        sysUser.setEmail(registerBody.getEmail());
        sysUser.setPhone(registerBody.getPhone());
        //默认启用状态
        sysUser.setStatus(1);
        //默认性别未知
        sysUser.setGender(0);

        //插入用户记录
        sysUserMapper.insert(sysUser);

        //分配默认角色（角色ID=1，即普通用户角色）
        SysUserRole sysUserRole = new SysUserRole();
        sysUserRole.setUserId(sysUser.getId());
        sysUserRole.setRoleId(1L);
        sysUserRoleMapper.insert(sysUserRole);

        log.info("用户注册成功: {}", registerBody.getUsername());
        return Result.success("注册成功", null);
    }
}
