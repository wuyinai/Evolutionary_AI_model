package com.example.evolutionary_ai_model.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.entity.dto.DeptAddDTO;
import com.example.evolutionary_ai_model.entity.dto.DeptUpdateDTO;
import com.example.evolutionary_ai_model.entity.SysDept;

import java.util.List;

/**
 * 用法：部门管理服务接口，定义部门的增删改查业务方法
 */
public interface SysDeptService {

    /**
     * 添加部门
     *
     * @param deptAddDTO 添加部门请求参数
     * @return 操作结果
     */
    Result<Void> addDept(DeptAddDTO deptAddDTO);

    /**
     * 修改部门
     *
     * @param deptUpdateDTO 修改部门请求参数
     * @return 操作结果
     */
    Result<Void> updateDept(DeptUpdateDTO deptUpdateDTO);

    /**
     * 删除部门（逻辑删除）
     *
     * @param deptId 部门ID
     * @return 操作结果
     */
    Result<Void> deleteDept(Long deptId);

    /**
     * 根据ID查询部门信息
     *
     * @param deptId 部门ID
     * @return 部门信息
     */
    Result<SysDept> getDeptById(Long deptId);

    /**
     * 查询所有部门列表（用于下拉选择）
     *
     * @return 部门列表
     */
    List<SysDept> listAllDepts();

    /**
     * 分页查询部门列表（支持模糊查询和条件筛选）
     *
     * @param page     页码
     * @param size     每页大小
     * @param deptName 部门名称（模糊查询）
     * @param deptCode 门编码（模糊查询）
     * @param status   状态（0-禁用，1-启用）
     * @param parentId 父部门ID
     * @return 部门分页数据
     */
    Page<SysDept> listDeptsPage(Integer page, Integer size, String deptName, String deptCode, Integer status, Long parentId);

    /**
     * 查询部门树形结构（用于前端展示）
     *
     * @param deptName 部门名称（模糊查询，可选）
     * @param deptCode 部门编码（模糊查询，可选）
     * @param status   状态（0-禁用，1-启用，可选）
     * @return 部门树形列表
     */
    List<SysDept> listDeptTree(String deptName, String deptCode, Integer status);

    /**
     * 批量关联用户到部门
     *
     * @param deptId  部门ID
     * @param userIds 用户ID列表
     * @return 操作结果
     */
    Result<Void> batchAssignUsers(Long deptId, List<Long> userIds);

    /**
     * 根据角色批量关联用户到部门
     *
     * @param deptId  部门ID
     * @param roleIds 角色ID列表
     * @return 操作结果
     */
    Result<Void> batchAssignUsersByRoles(Long deptId, List<Long> roleIds);

    /**
     * 移除用户与部门的关联
     *
     * @param userIds 用户ID列表
     * @return 操作结果
     */
    Result<Void> removeUsersFromDept(List<Long> userIds);

    /**
     * 查询部门下的用户列表
     *
     * @param deptId 部门ID
     * @return 用户ID列表
     */
    List<Long> listUsersByDeptId(Long deptId);
}
