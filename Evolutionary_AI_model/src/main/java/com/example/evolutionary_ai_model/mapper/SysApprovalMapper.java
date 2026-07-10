package com.example.evolutionary_ai_model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.evolutionary_ai_model.entity.SysApproval;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用法：审批Mapper接口，负责审批数据的持久化操作
 * 提供审批表的增删改查方法
 */
@Mapper
public interface SysApprovalMapper extends BaseMapper<SysApproval> {

    /**
     * 根据审批类型查询审批列表
     *
     * @param approvalType 审批类型
     * @return 审批列表
     */
    List<SysApproval> selectByApprovalType(@Param("approvalType") String approvalType);

    /**
     * 根据申请人ID查询审批列表
     *
     * @param applicantId 申请人ID
     * @return 审批列表
     */
    List<SysApproval> selectByApplicantId(@Param("applicantId") Long applicantId);

    /**
     * 根据审批状态查询审批列表
     *
     * @param approvalStatus 审批状态
     * @return 审批列表
     */
    List<SysApproval> selectByApprovalStatus(@Param("approvalStatus") Integer approvalStatus);
}