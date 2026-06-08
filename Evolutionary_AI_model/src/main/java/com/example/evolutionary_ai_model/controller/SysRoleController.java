package com.example.evolutionary_ai_model.controller;

import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.entity.dto.RoleAddDTO;
import com.example.evolutionary_ai_model.entity.dto.RoleUpdateDTO;
import com.example.evolutionary_ai_model.entity.SysRole;
import com.example.evolutionary_ai_model.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用法：角色管理控制器，提供角色的增删改查REST接口，需要相应权限才能访问
 */
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class SysRoleController {

    //角色管理服务，处理角色增删改查业务逻辑
    private final SysRoleService sysRoleService;

    /**
     * 添加角色
     */
    @PostMapping
    //需要 sys:role:add 权限才能访问
    @PreAuthorize("hasAuthority('sys:role:add')")
    public Result<Void> addRole(@RequestBody @Validated RoleAddDTO roleAddDTO) {
        return sysRoleService.addRole(roleAddDTO);
    }

    /**
     * 修改角色
     */
    @PutMapping
    //需要 sys:role:edit 权限才能访问
    @PreAuthorize("hasAuthority('sys:role:edit')")
    public Result<Void> updateRole(@RequestBody @Validated RoleUpdateDTO roleUpdateDTO) {
        return sysRoleService.updateRole(roleUpdateDTO);
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{roleId}")
    //需要 sys:role:delete 权限才能访问
    @PreAuthorize("hasAuthority('sys:role:delete')")
    public Result<Void> deleteRole(@PathVariable Long roleId) {
        return sysRoleService.deleteRole(roleId);
    }

    /**
     * 根据ID查询角色信息
     */
    @GetMapping("/{roleId}")
    //需要 sys:role:list 权限才能访问
    @PreAuthorize("hasAuthority('sys:role:list')")
    public Result<SysRole> getRoleById(@PathVariable Long roleId) {
        return sysRoleService.getRoleById(roleId);
    }
}
