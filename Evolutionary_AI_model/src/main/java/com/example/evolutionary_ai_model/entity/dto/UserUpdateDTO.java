package com.example.evolutionary_ai_model.entity.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

/**
 * 用法：修改用户信息时的请求参数封装，不包含密码字段（密码修改应走独立的重置密码接口）
 */
@Data
public class UserUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    //用户ID，必填，标识要修改的用户
    @NotNull(message = "用户ID不能为空")
    private Long id;

    //真实姓名，选填
    private String realName;

    //邮箱，选填，符合邮箱格式
    @Pattern(regexp = "^$|^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "邮箱格式不正确")
    private String email;

    //手机号，选填，符合手机号格式
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    //性别：0-未知，1-男，2-女
    private Integer gender;

    //状态：0-禁用，1-启用
    private Integer status;

    //部门ID，选填
    private Long deptId;

    //备注，选填
    private String remark;

    //头像URL，选填
    private String avatar;
}
