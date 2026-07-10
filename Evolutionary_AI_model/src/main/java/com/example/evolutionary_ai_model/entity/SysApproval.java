package com.example.evolutionary_ai_model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用法：审批实体类，对应数据库表sys_approval
 * 用于存储角色创建审批、角色用户授权审批、部门用户变动审批等各类审批记录
 */
@Data
@TableName("sys_approval")
public class SysApproval implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    // 审批类型：role_create-角色创建审批，role_user_auth-角色用户授权审批，dept_user_change-部门用户变动审批
    private String approvalType;

    // 审批标题
    private String approvalTitle;

    // 审批内容（JSON格式存储详细内容）
    private String approvalContent;

    // 申请人ID
    private Long applicantId;

    // 申请人姓名
    private String applicantName;

    // 审批人ID
    private Long approverId;

    // 审批人姓名
    private String approverName;

    // 审批状态：0-待审批，1-已通过，2-已拒绝
    private Integer approvalStatus;

    // 审批时间
    private LocalDateTime approvalTime;

    // 审批意见
    private String approvalOpinion;

    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer delFlag;

    private String remark;
}