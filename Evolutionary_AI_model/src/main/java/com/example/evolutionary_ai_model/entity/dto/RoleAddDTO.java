package com.example.evolutionary_ai_model.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 用法：添加角色时的请求参数封装
 */
@Data
public class RoleAddDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    //角色名称，必填
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    private String roleName;

    //角色编码，必填，如 ROLE_ADMIN
    @NotBlank(message = "角色编码不能为空")
    @Size(max = 100, message = "角色编码长度不能超过100个字符")
    private String roleCode;

    //显示顺序
    private Integer roleSort;

    //数据范围：1-全部数据，2-本部门数据，3-本部门及以下数据，4-仅本人数据，5-自定义
    private Integer dataScope;

    //状态：0-禁用，1-启用
    private Integer status;

    //备注
    private String remark;
}
