package com.example.evolutionary_ai_model.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.entity.dto.RoleAddDTO;
import com.example.evolutionary_ai_model.entity.dto.RoleUpdateDTO;
import com.example.evolutionary_ai_model.entity.SysRole;
import com.example.evolutionary_ai_model.entity.SysUser;

import java.util.List;

/**
 * 用法：角色管理服务接口，定义角色的增删改查业务方法
 */
public interface SysRoleService {

    /**
     * 分页查询角色列表
     *
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    Result<Page<SysRole>> listRoles(int page, int size);

    /**
     * 添加角色
     *
     * @param roleAddDTO 添加角色请求参数
     * @return 操作结果
     */
    Result<Void> addRole(RoleAddDTO roleAddDTO);

    /**
     * 修改角色
     *
     * @param roleUpdateDTO 修改角色请求参数
     * @return 操作结果
     */
    Result<Void> updateRole(RoleUpdateDTO roleUpdateDTO);

    /**
     * 删除角色（逻辑删除）
     *
     * @param roleId 角色ID
     * @return 操作结果
     */
    Result<Void> deleteRole(Long roleId);

    /**
     * 根据ID查询角色信息
     *
     * @param roleId 角色ID
     * @return 角色信息
     */
    Result<SysRole> getRoleById(Long roleId);

    /**
     * 查询角色下的用户列表
     *
     * @param roleId 角色ID
     * @return 用户列表
     */
    Result<List<SysUser>> getUsersByRoleId(Long roleId);

    /**
     * 为角色分配用户（批量添加用户到角色）
     *
     * @param roleId 角色ID
     * @param userIds 用户ID列表
     * @return 操作结果
     */
    Result<Void> assignUsersToRole(Long roleId, List<Long> userIds);

    /**
     * 从角色中移除用户
     *
     * @param roleId 角色ID
     * @param userId 用户ID
     * @return 操作结果
     */
    Result<Void> removeUserFromRole(Long roleId, Long userId);

    /**
     * 批量从角色中移除用户
     *
     * @param roleId 角色ID
     * @param userIds 用户ID列表
     * @return 操作结果
     */
    Result<Void> removeUsersFromRole(Long roleId, List<Long> userIds);
}
