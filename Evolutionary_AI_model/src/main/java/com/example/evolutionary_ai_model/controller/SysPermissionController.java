package com.example.evolutionary_ai_model.controller;

import com.example.evolutionary_ai_model.annotation.OperationLog;
import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.entity.SysPermission;
import com.example.evolutionary_ai_model.entity.dto.PermissionAddDTO;
import com.example.evolutionary_ai_model.entity.dto.PermissionUpdateDTO;
import com.example.evolutionary_ai_model.service.SysPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单/权限管理控制器，提供菜单权限的增删改查REST接口，需要相应权限才能访问
 */
@RestController
@RequestMapping("/system/permission")
@RequiredArgsConstructor
public class SysPermissionController {

    private final SysPermissionService sysPermissionService;

    /**
     * 查询所有菜单/权限列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('sys:permission:list')")
    public Result<List<SysPermission>> listAllPermissions() {
        return sysPermissionService.listAllPermissions();
    }

    /**
     * 获取当前登录用户的菜单树（用于动态渲染侧边栏）
     * 不需要特定权限，登录即可访问
     */
    @GetMapping("/user-menu")
    public Result<List<SysPermission>> getUserMenuTree() {
        return sysPermissionService.getUserMenuTree();
    }

    /**
     * 添加菜单/权限
     */
    @PostMapping
    @PreAuthorize("hasAuthority('sys:permission:add')")
    @OperationLog("添加菜单/权限")
    public Result<Void> addPermission(@RequestBody @Validated PermissionAddDTO addDTO) {
        return sysPermissionService.addPermission(addDTO);
    }

    /**
     * 修改菜单/权限
     */
    @PutMapping
    @PreAuthorize("hasAuthority('sys:permission:edit')")
    @OperationLog("修改菜单/权限")
    public Result<Void> updatePermission(@RequestBody @Validated PermissionUpdateDTO updateDTO) {
        return sysPermissionService.updatePermission(updateDTO);
    }

    /**
     * 删除菜单/权限
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:permission:delete')")
    @OperationLog("删除菜单/权限")
    public Result<Void> deletePermission(@PathVariable Long id) {
        return sysPermissionService.deletePermission(id);
    }

    /**
     * 根据ID查询菜单/权限详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:permission:list')")
    public Result<SysPermission> getPermissionById(@PathVariable Long id) {
        return sysPermissionService.getPermissionById(id);
    }

    /**
     * 获取角色已分配的权限ID列表
     */
    @GetMapping("/role/{roleId}")
    @PreAuthorize("hasAuthority('sys:role:list')")
    public Result<List<Long>> getRolePermissionIds(@PathVariable Long roleId) {
        return sysPermissionService.getRolePermissionIds(roleId);
    }

    /**
     * 更新角色的权限分配
     */
    @PutMapping("/role/{roleId}")
    @PreAuthorize("hasAuthority('sys:role:edit')")
    @OperationLog("分配角色权限")
    public Result<Void> updateRolePermissions(@PathVariable Long roleId, @RequestBody List<Long> permissionIds) {
        return sysPermissionService.updateRolePermissions(roleId, permissionIds);
    }
}
