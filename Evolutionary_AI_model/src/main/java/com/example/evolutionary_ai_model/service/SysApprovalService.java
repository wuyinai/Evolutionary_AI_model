package com.example.evolutionary_ai_model.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.entity.SysApproval;
import com.example.evolutionary_ai_model.entity.SysDict;

import java.util.List;

/**
 * 用法：审批管理服务接口，定义审批的查询业务方法
 */
public interface SysApprovalService {

    /**
     * 分页查询审批列表
     *
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    Result<Page<SysApproval>> listApprovals(int page, int size);

    /**
     * 根据审批类型分页查询审批列表
     *
     * @param approvalType 审批类型
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    Result<Page<SysApproval>> listApprovalsByType(String approvalType, int page, int size);

    /**
     * 根据审批状态分页查询审批列表
     *
     * @param approvalStatus 审批状态
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    Result<Page<SysApproval>> listApprovalsByStatus(Integer approvalStatus, int page, int size);

    /**
     * 根据申请人ID分页查询审批列表
     *
     * @param applicantId 申请人ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    Result<Page<SysApproval>> listApprovalsByApplicant(Long applicantId, int page, int size);

    /**
     * 根据ID查询审批详情
     *
     * @param approvalId 审批ID
     * @return 审批详情
     */
    Result<SysApproval> getApprovalById(Long approvalId);

    /**
     * 查询审批类型字典列表
     *
     * @return 审批类型字典列表
     */
    Result<List<SysDict>> getApprovalTypes();

    /**
     * 查询审批状态字典列表
     *
     * @return 审批状态字典列表
     */
    Result<List<SysDict>> getApprovalStatuses();
}