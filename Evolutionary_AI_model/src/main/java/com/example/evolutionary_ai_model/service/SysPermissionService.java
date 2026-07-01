package com.example.evolutionary_ai_model.service;

import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.entity.SysPermission;
import com.example.evolutionary_ai_model.entity.dto.PermissionAddDTO;
import com.example.evolutionary_ai_model.entity.dto.PermissionUpdateDTO;

import java.util.List;

/**
 * 菜单/权限管理服务接口，定义菜单权限的增删改查业务方法
 */
public interface SysPermissionService {

    /**
     * 查询所有菜单/权限列表（按父ID和排序排列）
     */
    Result<List<SysPermission>> listAllPermissions();

    /**
     * 添加菜单/权限
     */
    Result<Void> addPermission(PermissionAddDTO addDTO);

    /**
     * 修改菜单/权限
     */
    Result<Void> updatePermission(PermissionUpdateDTO updateDTO);

    /**
     * 删除菜单/权限（逻辑删除）
     */
    Result<Void> deletePermission(Long id);

    /**
     * 根据ID查询菜单/权限
     */
    Result<SysPermission> getPermissionById(Long id);

    /**
     * 获取当前登录用户的菜单树（仅返回目录和菜单类型，过滤按钮权限）
     */
    Result<List<SysPermission>> getUserMenuTree();

    /**
     * 获取角色已分配的权限ID列表
     */
    Result<List<Long>> getRolePermissionIds(Long roleId);

    /**
     * 更新角色的权限分配
     */
    Result<Void> updateRolePermissions(Long roleId, List<Long> permissionIds);

    /**
     * 获取当前登录用户的权限码列表（用于前端按钮权限控制）
     */
    Result<List<String>> getUserPermissionCodes();
}
