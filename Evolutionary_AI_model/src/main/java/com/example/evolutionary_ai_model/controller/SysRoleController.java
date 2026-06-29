package com.example.evolutionary_ai_model.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.evolutionary_ai_model.annotation.OperationLog;
import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.entity.dto.RoleAddDTO;
import com.example.evolutionary_ai_model.entity.dto.RoleUpdateDTO;
import com.example.evolutionary_ai_model.entity.SysRole;
import com.example.evolutionary_ai_model.entity.SysUser;
import com.example.evolutionary_ai_model.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * 分页查询角色列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('sys:role:list')")
    public Result<Page<SysRole>> listRoles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return sysRoleService.listRoles(page, size);
    }

    /**
     * 添加角色
     */
    @PostMapping
    //需要 sys:role:add 权限才能访问
    @PreAuthorize("hasAuthority('sys:role:add')")
    @OperationLog("添加角色")
    public Result<Void> addRole(@RequestBody @Validated RoleAddDTO roleAddDTO) {
        return sysRoleService.addRole(roleAddDTO);
    }

    /**
     * 修改角色
     */
    @PutMapping
    //需要 sys:role:edit 权限才能访问
    @PreAuthorize("hasAuthority('sys:role:edit')")
    @OperationLog("修改角色")
    public Result<Void> updateRole(@RequestBody @Validated RoleUpdateDTO roleUpdateDTO) {
        return sysRoleService.updateRole(roleUpdateDTO);
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{roleId}")
    //需要 sys:role:delete 权限才能访问
    @PreAuthorize("hasAuthority('sys:role:delete')")
    @OperationLog("删除角色")
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

    /**
     * 查询角色下的用户列表
     */
    @GetMapping("/{roleId}/users")
    @PreAuthorize("hasAuthority('sys:role:list')")
    public Result<List<SysUser>> getUsersByRoleId(@PathVariable Long roleId) {
        return sysRoleService.getUsersByRoleId(roleId);
    }

    /**
     * 为角色分配用户（批量添加用户到角色）
     */
    @PostMapping("/{roleId}/users")
    @PreAuthorize("hasAuthority('sys:role:edit')")
    @OperationLog("为角色分配用户")
    public Result<Void> assignUsersToRole(@PathVariable Long roleId, @RequestBody List<Long> userIds) {
        return sysRoleService.assignUsersToRole(roleId, userIds);
    }

    /**
     * 从角色中移除用户
     */
    @DeleteMapping("/{roleId}/users/{userId}")
    @PreAuthorize("hasAuthority('sys:role:edit')")
    @OperationLog("从角色移除用户")
    public Result<Void> removeUserFromRole(@PathVariable Long roleId, @PathVariable Long userId) {
        return sysRoleService.removeUserFromRole(roleId, userId);
    }

    /**
     * 批量从角色中移除用户
     */
    @DeleteMapping("/{roleId}/users")
    @PreAuthorize("hasAuthority('sys:role:edit')")
    @OperationLog("批量从角色移除用户")
    public Result<Void> removeUsersFromRole(@PathVariable Long roleId, @RequestBody List<Long> userIds) {
        return sysRoleService.removeUsersFromRole(roleId, userIds);
    }
}
