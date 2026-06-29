package com.example.evolutionary_ai_model.service;

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
}
