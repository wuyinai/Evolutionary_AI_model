package com.example.evolutionary_ai_model.service;

import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.entity.dto.RoleAddDTO;
import com.example.evolutionary_ai_model.entity.dto.RoleUpdateDTO;
import com.example.evolutionary_ai_model.entity.SysRole;

/**
 * 用法：角色管理服务接口，定义角色的增删改查业务方法
 */
public interface SysRoleService {

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
}
