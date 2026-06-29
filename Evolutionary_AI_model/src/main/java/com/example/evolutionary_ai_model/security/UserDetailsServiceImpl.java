package com.example.evolutionary_ai_model.security;

import com.example.evolutionary_ai_model.entity.SysPermission;
import com.example.evolutionary_ai_model.entity.SysUser;
import com.example.evolutionary_ai_model.mapper.SysPermissionMapper;
import com.example.evolutionary_ai_model.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


/**
 * 用处 ：实现 SpringSecurity 的 UserDetailsService 接口，是认证流程的核心：
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper sysUserMapper;
    private final SysPermissionMapper sysPermissionMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //查询用户
        SysUser sysUser = sysUserMapper.selectByUsername(username);
        if (sysUser == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        //查询用户关联的权限列表
        List<SysPermission> permissions = sysPermissionMapper.selectPermissionsByUserId(sysUser.getId());
        List<String> permissionCodes = permissions.stream()
                .map(SysPermission::getPermissionCode)
                .filter(code -> code != null && !code.trim().isEmpty())
                .collect(Collectors.toList());
        //封装成 UserDetails 返回
        return new LoginUserDetails(sysUser, permissionCodes);
    }
}
