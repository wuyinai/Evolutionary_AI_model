package com.example.evolutionary_ai_model.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.evolutionary_ai_model.annotation.OperationLog;
import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.entity.dto.UserAddDTO;
import com.example.evolutionary_ai_model.entity.dto.UserUpdateDTO;
import com.example.evolutionary_ai_model.entity.SysUser;
import com.example.evolutionary_ai_model.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用法：用户管理控制器，提供用户的增删改查REST接口，需要相应权限才能访问
 */
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class SysUserController {

    //用户管理服务，处理用户增删改查业务逻辑
    private final SysUserService sysUserService;

    /**
     * 分页查询用户列表（支持部门筛选）
     * 用于角色分配用户功能，不做权限限制
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('sys:user:list')")
    public Result<Page<SysUser>> listUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long deptId) {
        return Result.success(sysUserService.listUsers(page, size, deptId));
    }

    /**
     * 添加用户
     */
    @PostMapping
    //需要 sys:user:add 权限才能访问
    @PreAuthorize("hasAuthority('sys:user:add')")
    @OperationLog("添加用户")
    public Result<Void> addUser(@RequestBody @Validated UserAddDTO userAddDTO) {
        return sysUserService.addUser(userAddDTO);
    }

    /**
     * 修改用户信息
     */
    @PutMapping
    //需要 sys:user:edit 权限才能访问
    @PreAuthorize("hasAuthority('sys:user:edit')")
    @OperationLog("修改用户")
    public Result<Void> updateUser(@RequestBody @Validated UserUpdateDTO userUpdateDTO) {
        return sysUserService.updateUser(userUpdateDTO);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    //需要 sys:user:delete 权限才能访问
    @PreAuthorize("hasAuthority('sys:user:delete')")
    @OperationLog("删除用户")
    public Result<Void> deleteUser(@PathVariable Long userId) {
        return sysUserService.deleteUser(userId);
    }

    /**
     * 根据ID查询用户信息
     */
    @GetMapping("/{userId}")
    //需要 sys:user:list 权限才能访问
    @PreAuthorize("hasAuthority('sys:user:list')")
    public Result<SysUser> getUserById(@PathVariable Long userId) {
        return sysUserService.getUserById(userId);
    }
}
