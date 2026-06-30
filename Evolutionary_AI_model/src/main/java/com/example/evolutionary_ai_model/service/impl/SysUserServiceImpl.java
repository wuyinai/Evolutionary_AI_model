package com.example.evolutionary_ai_model.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.entity.dto.UserAddDTO;
import com.example.evolutionary_ai_model.entity.dto.UserUpdateDTO;
import com.example.evolutionary_ai_model.entity.SysUser;
import com.example.evolutionary_ai_model.entity.SysUserRole;
import com.example.evolutionary_ai_model.mapper.SysUserRoleMapper;
import com.example.evolutionary_ai_model.mapper.SysUserMapper;
import com.example.evolutionary_ai_model.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用法：用户管理服务实现类，处理用户的增删改查业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    //密码加密器，用于BCrypt加密
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> addUser(UserAddDTO userAddDTO) {
        //校验用户名是否已存在
        SysUser existUser = sysUserMapper.selectByUsername(userAddDTO.getUsername());
        if (existUser != null) {
            return Result.fail("用户名已存在");
        }

        //构建用户实体并加密密码
        SysUser sysUser = new SysUser();
        sysUser.setUsername(userAddDTO.getUsername());
        sysUser.setPassword(passwordEncoder.encode(userAddDTO.getPassword()));
        sysUser.setRealName(userAddDTO.getRealName());
        sysUser.setEmail(userAddDTO.getEmail());
        sysUser.setPhone(userAddDTO.getPhone());
        sysUser.setGender(userAddDTO.getGender() != null ? userAddDTO.getGender() : 0);
        sysUser.setStatus(userAddDTO.getStatus() != null ? userAddDTO.getStatus() : 1);
        sysUser.setDeptId(userAddDTO.getDeptId());
        sysUser.setRemark(userAddDTO.getRemark());

        //插入用户记录
        sysUserMapper.insert(sysUser);

        //分配默认角色（角色ID=1，即普通用户角色）
//        SysUserRole sysUserRole = new SysUserRole();
//        sysUserRole.setUserId(sysUser.getId());
//        sysUserRole.setRoleId(1L);
//        sysUserRoleMapper.insert(sysUserRole);

        log.info("管理员添加用户成功: {}", userAddDTO.getUsername());
        return Result.success("添加用户成功", null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> updateUser(UserUpdateDTO userUpdateDTO) {
        //校验用户是否存在
        SysUser existUser = sysUserMapper.selectById(userUpdateDTO.getId());
        if (existUser == null) {
            return Result.fail("用户不存在");
        }

        //构建更新实体，只设置非空字段
        SysUser sysUser = new SysUser();
        sysUser.setId(userUpdateDTO.getId());
        sysUser.setRealName(userUpdateDTO.getRealName());
        sysUser.setEmail(userUpdateDTO.getEmail());
        sysUser.setPhone(userUpdateDTO.getPhone());
        sysUser.setGender(userUpdateDTO.getGender());
        sysUser.setStatus(userUpdateDTO.getStatus());
        sysUser.setDeptId(userUpdateDTO.getDeptId());
        sysUser.setRemark(userUpdateDTO.getRemark());

        //更新用户记录
        sysUserMapper.updateById(sysUser);

        log.info("修改用户信息成功, userId: {}", userUpdateDTO.getId());
        return Result.success("修改用户成功", null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteUser(Long userId) {
        //校验用户是否存在
        SysUser existUser = sysUserMapper.selectById(userId);
        if (existUser == null) {
            return Result.fail("用户不存在");
        }

        //逻辑删除用户（MyBatis-Plus @TableLogic 自动将 del_flag 设为1）
        sysUserMapper.deleteById(userId);

        log.info("删除用户成功, userId: {}", userId);
        return Result.success("删除用户成功", null);
    }

    @Override
    public Result<SysUser> getUserById(Long userId) {
        SysUser sysUser = sysUserMapper.selectById(userId);
        if (sysUser == null) {
            return Result.fail("用户不存在");
        }

        //脱敏：清空密码字段，不返回给前端
        sysUser.setPassword(null);

        return Result.success(sysUser);
    }

    @Override
    public Page<SysUser> listUsers(Integer page, Integer size, Long deptId) {
        //构建分页对象
        Page<SysUser> pageObj = new Page<>(page != null ? page : 1, size != null ? size : 10);

        //构建查询条件
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(deptId != null, SysUser::getDeptId, deptId);
        wrapper.orderByDesc(SysUser::getCreateTime);

        //执行分页查询
        Page<SysUser> result = sysUserMapper.selectPage(pageObj, wrapper);

        //脱敏：清空密码字段
        result.getRecords().forEach(user -> user.setPassword(null));

        return result;
    }
}
