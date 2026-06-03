package com.example.evolutionary_ai_model.service;

import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.dto.UserAddDTO;
import com.example.evolutionary_ai_model.dto.UserUpdateDTO;
import com.example.evolutionary_ai_model.entity.SysUser;

/**
 * 用法：用户管理服务接口，定义用户的增删改查业务方法
 */
public interface SysUserService {

    /**
     * 添加用户
     *
     * @param userAddDTO 添加用户请求参数
     * @return 操作结果
     */
    Result<Void> addUser(UserAddDTO userAddDTO);

    /**
     * 修改用户信息
     *
     * @param userUpdateDTO 修改用户请求参数
     * @return 操作结果
     */
    Result<Void> updateUser(UserUpdateDTO userUpdateDTO);

    /**
     * 删除用户（逻辑删除）
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    Result<Void> deleteUser(Long userId);

    /**
     * 根据ID查询用户信息
     *
     * @param userId 用户ID
     * @return 用户信息（脱敏）
     */
    Result<SysUser> getUserById(Long userId);
}
