package com.example.evolutionary_ai_model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用法：部门实体类，对应 sys_dept 表
 */
@Data
@TableName("sys_dept")
public class SysDept implements Serializable {

    private static final long serialVersionUID = 1L;

    //部门ID
    @TableId
    private Long id;

    //父部门ID
    private Long parentId;

    //祖级列表
    private String ancestors;

    //部门名称
    private String deptName;

    //部门编码
    private String deptCode;

    //显示顺序
    private Integer sort;

    //负责人姓名
    private String leader;

    //负责人用户ID
    private Long leaderId;

    //联系电话
    private String phone;

    //邮箱
    private String email;

    //状态：0-禁用，1-启用
    private Integer status;

    //创建人
    private String createBy;

    //创建时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    //修改人
    private String updateBy;

    //修改时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    //删除标记：0-未删除，1-已删除
    @TableLogic
    private Integer delFlag;

    //备注
    private String remark;
}
