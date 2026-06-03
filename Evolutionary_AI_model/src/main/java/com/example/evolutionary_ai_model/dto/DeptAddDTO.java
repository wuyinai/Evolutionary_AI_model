package com.example.evolutionary_ai_model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 用法：添加部门时的请求参数封装
 */
@Data
public class DeptAddDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    //父部门ID，必填（顶级部门传0）
    @NotNull(message = "父部门ID不能为空")
    private Long parentId;

    //部门名称，必填
    @NotBlank(message = "部门名称不能为空")
    @Size(max = 50, message = "部门名称长度不能超过50个字符")
    private String deptName;

    //部门编码，选填
    @Size(max = 100, message = "部门编码长度不能超过100个字符")
    private String deptCode;

    //显示顺序
    private Integer sort;

    //负责人
    private String leader;

    //联系电话
    private String phone;

    //邮箱
    private String email;

    //状态：0-禁用，1-启用
    private Integer status;

    //备注
    private String remark;
}
