package com.example.evolutionary_ai_model.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 用法：添加菜单/权限时的请求参数封装
 */
@Data
public class PermissionAddDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    //父菜单ID，0表示根菜单
    @NotNull(message = "父菜单ID不能为空")
    private Long parentId;

    //菜单/权限名称，必填
    @NotBlank(message = "名称不能为空")
    @Size(max = 50, message = "名称长度不能超过50个字符")
    private String permissionName;

    //权限标识，如 sys:menu:list（按钮类型必填）
    private String permissionCode;

    //类型：1-目录，2-菜单，3-按钮，必填
    @NotNull(message = "类型不能为空")
    private Integer permissionType;

    //路由路径（目录/菜单类型必填）
    private String path;

    //组件路径（菜单类型必填）
    private String component;

    //图标（目录/菜单类型可用）
    private String icon;

    //排序
    private Integer sort;

    //是否可见：0-显示，1-隐藏
    private Integer visible;

    //状态：0-禁用，1-启用
    private Integer status;

    //备注
    private String remark;
}
