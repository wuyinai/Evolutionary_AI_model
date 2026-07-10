package com.example.evolutionary_ai_model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_role")
public class SysRole implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private String roleName;

    private String roleCode;

    private Integer roleSort;

    private Integer dataScope;

    /**
     * 权限控制开关：0-禁用，1-启用
     * 禁用时该角色不受权限管控，默认拥有全部权限
     */
    private Integer permControl;

    private Integer status;

    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer delFlag;
    // 新增字段
    @JsonSerialize(using = ToStringSerializer.class)
    private Long securityLabelId;

    private String remark;
}
