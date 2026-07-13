package com.example.evolutionary_ai_model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户部门关联实体，对应 sys_user_dept 表，用于给用户分配多个部门
 */
@Data
@TableName("sys_user_dept")
public class SysUserDept implements Serializable {

    private static final long serialVersionUID = 1L;

    // 主键ID，自增
    @TableId
    private Long id;

    // 用户ID
    private Long userId;

    // 部门ID
    private Long deptId;

    // 创建人
    private String createBy;

    // 创建时间
    private LocalDateTime createTime;
}
