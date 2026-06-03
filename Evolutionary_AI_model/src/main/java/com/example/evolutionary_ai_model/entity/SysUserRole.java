package com.example.evolutionary_ai_model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用法：用户角色关联实体，对应 sys_user_role 表，用于给用户分配角色
 */
@Data
@TableName("sys_user_role")
public class SysUserRole implements Serializable {

    private static final long serialVersionUID = 1L;

    //主键ID，自增
    @TableId
    private Long id;

    //用户ID
    private Long userId;

    //角色ID
    private Long roleId;

    //创建时间
    private LocalDateTime createTime;
}
