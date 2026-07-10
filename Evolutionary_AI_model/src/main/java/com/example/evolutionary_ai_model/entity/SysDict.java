package com.example.evolutionary_ai_model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用法：字典实体类，对应数据库表sys_dict
 * 用于存储系统字典数据，如审批类型、审批状态等
 */
@Data
@TableName("sys_dict")
public class SysDict implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    // 字典类型
    private String dictType;

    // 字典名称（中文名称）
    private String dictName;

    // 字典编码
    private String dictCode;

    // 字典标签
    private String dictLabel;

    // 字典值
    private String dictValue;

    // 排序
    private Integer sort;

    // 状态：0-禁用，1-启用
    private Integer status;

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