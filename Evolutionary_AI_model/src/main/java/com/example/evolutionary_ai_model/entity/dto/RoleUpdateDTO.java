package com.example.evolutionary_ai_model.entity.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 用法：修改角色时的请求参数封装
 */
@Data
public class RoleUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    //角色ID，必填，标识要修改的角色
    @NotNull(message = "角色ID不能为空")
    private Long id;

    //角色名称，选填
    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    private String roleName;

    //角色编码，选填
    @Size(max = 100, message = "角色编码长度不能超过100个字符")
    private String roleCode;

    //显示顺序
    private Integer roleSort;

    //数据范围
    private Integer dataScope;

    //状态：0-禁用，1-启用
    private Integer status;

    //备注
    private String remark;
}
