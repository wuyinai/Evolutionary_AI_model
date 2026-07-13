package com.example.evolutionary_ai_model.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 用法：管理员添加用户时的请求参数封装
 */
@Data
public class UserAddDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    //用户名，必填，长度3~20
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3到20个字符之间")
    private String username;

    //密码，必填，长度6~20，需包含字母和数字
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6到20个字符之间")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "密码必须包含字母和数字")
    private String password;

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

    //备注，选填
    private String remark;

    //头像URL，选填
    private String avatar;
}
