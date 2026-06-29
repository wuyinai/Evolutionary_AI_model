package com.example.evolutionary_ai_model.security;

import com.example.evolutionary_ai_model.entity.SysUser;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 实现 SpringSecurity 的 UserDetails 接口，
 * 将数据库中的 SysUser 和权限列表包装成 SpringSecurity 能识别的身份对象：
 */
@Data
public class LoginUserDetails implements UserDetails {

    private static final long serialVersionUID = 1L;

    private final SysUser sysUser;
    private final List<String> permissionCodes;

    public LoginUserDetails(SysUser sysUser, List<String> permissionCodes) {
        this.sysUser = sysUser;
        this.permissionCodes = permissionCodes;
    }

    //getAuthorities()：将权限编码列表转为SimpleGrantedAuthority ，用于 @PreAuthorize 权限判断
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return permissionCodes.stream()
                .filter(code -> code != null && !code.trim().isEmpty())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return sysUser.getPassword();
    }

    @Override
    public String getUsername() {
        return sysUser.getUsername();
    }

    //isAccountNonExpired() ：根据 account_expire_time 判断账号是否过期
    @Override
    public boolean isAccountNonExpired() {
        if (sysUser.getAccountExpireTime() == null) {
            return true;
        }
        return sysUser.getAccountExpireTime().isAfter(java.time.LocalDateTime.now());
    }

    //isAccountNonLocked() / isEnabled() ：根据 status 字段判断账号是否锁定/启用
    @Override
    public boolean isAccountNonLocked() {
        return sysUser.getStatus() != null && sysUser.getStatus() == 1;
    }

    //isCredentialsNonExpired() ：根据 credentials_expire_time 判断密码是否过期
    @Override
    public boolean isCredentialsNonExpired() {
        if (sysUser.getCredentialsExpireTime() == null) {
            return true;
        }
        return sysUser.getCredentialsExpireTime().isAfter(java.time.LocalDateTime.now());
    }

    @Override
    public boolean isEnabled() {
        return sysUser.getStatus() != null && sysUser.getStatus() == 1;
    }

    public Long getUserId() {
        return sysUser.getId();
    }
}
