package com.example.evolutionary_ai_model.entity.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 用法：修改菜单/权限时的请求参数封装
 */
@Data
public class PermissionUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    //权限ID，必填，标识要修改的权限
    @NotNull(message = "权限ID不能为空")
    private Long id;

    //父菜单ID
    private Long parentId;

    //菜单/权限名称
    @Size(max = 50, message = "名称长度不能超过50个字符")
    private String permissionName;

    //权限标识
    private String permissionCode;

    //类型：1-目录，2-菜单，3-按钮
    private Integer permissionType;

    //路由路径
    private String path;

    //组件路径
    private String component;

    //图标
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
