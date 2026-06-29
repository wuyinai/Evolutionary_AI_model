package com.example.evolutionary_ai_model.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.entity.dto.RoleAddDTO;
import com.example.evolutionary_ai_model.entity.dto.RoleUpdateDTO;
import com.example.evolutionary_ai_model.entity.SysRole;
import com.example.evolutionary_ai_model.entity.SysUser;
import com.example.evolutionary_ai_model.entity.SysUserRole;
import com.example.evolutionary_ai_model.mapper.SysRoleMapper;
import com.example.evolutionary_ai_model.mapper.SysUserMapper;
import com.example.evolutionary_ai_model.mapper.SysUserRoleMapper;
import com.example.evolutionary_ai_model.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用法：角色管理服务实现类，处理角色的增删改查业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl implements SysRoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    @Override
    public Result<Page<SysRole>> listRoles(int page, int size) {
        Page<SysRole> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(SysRole::getRoleSort);
        Page<SysRole> result = sysRoleMapper.selectPage(pageParam, queryWrapper);
        return Result.success(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> addRole(RoleAddDTO roleAddDTO) {
        //校验角色编码是否已存在
        Long count = sysRoleMapper.selectCount(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, roleAddDTO.getRoleCode())
        );
        if (count > 0) {
            return Result.fail("角色编码已存在");
        }

        //构建角色实体
        SysRole sysRole = new SysRole();
        sysRole.setRoleName(roleAddDTO.getRoleName());
        sysRole.setRoleCode(roleAddDTO.getRoleCode());
        sysRole.setRoleSort(roleAddDTO.getRoleSort() != null ? roleAddDTO.getRoleSort() : 0);
        sysRole.setDataScope(roleAddDTO.getDataScope() != null ? roleAddDTO.getDataScope() : 1);
        sysRole.setStatus(roleAddDTO.getStatus() != null ? roleAddDTO.getStatus() : 1);
        sysRole.setRemark(roleAddDTO.getRemark());

        //插入角色记录
        sysRoleMapper.insert(sysRole);

        log.info("添加角色成功: {}", roleAddDTO.getRoleCode());
        return Result.success("添加角色成功", null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> updateRole(RoleUpdateDTO roleUpdateDTO) {
        //校验角色是否存在
        SysRole existRole = sysRoleMapper.selectById(roleUpdateDTO.getId());
        if (existRole == null) {
            return Result.fail("角色不存在");
        }

        //如果修改了角色编码，校验新编码是否已被占用
        if (roleUpdateDTO.getRoleCode() != null
                && !roleUpdateDTO.getRoleCode().equals(existRole.getRoleCode())) {
            Long count = sysRoleMapper.selectCount(
                    new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, roleUpdateDTO.getRoleCode())
            );
            if (count > 0) {
                return Result.fail("角色编码已存在");
            }
        }

        //构建更新实体
        SysRole sysRole = new SysRole();
        sysRole.setId(roleUpdateDTO.getId());
        sysRole.setRoleName(roleUpdateDTO.getRoleName());
        sysRole.setRoleCode(roleUpdateDTO.getRoleCode());
        sysRole.setRoleSort(roleUpdateDTO.getRoleSort());
        sysRole.setDataScope(roleUpdateDTO.getDataScope());
        sysRole.setStatus(roleUpdateDTO.getStatus());
        sysRole.setRemark(roleUpdateDTO.getRemark());

        //更新角色记录
        sysRoleMapper.updateById(sysRole);

        log.info("修改角色成功, roleId: {}", roleUpdateDTO.getId());
        return Result.success("修改角色成功", null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteRole(Long roleId) {
        //校验角色是否存在
        SysRole existRole = sysRoleMapper.selectById(roleId);
        if (existRole == null) {
            return Result.fail("角色不存在");
        }

        //删除角色与用户的关联关系
        sysUserRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, roleId)
        );

        //逻辑删除角色
        sysRoleMapper.deleteById(roleId);

        log.info("删除角色成功, roleId: {}", roleId);
        return Result.success("删除角色成功", null);
    }

    @Override
    public Result<SysRole> getRoleById(Long roleId) {
        SysRole sysRole = sysRoleMapper.selectById(roleId);
        if (sysRole == null) {
            return Result.fail("角色不存在");
        }
        return Result.success(sysRole);
    }

    @Override
    public Result<List<SysUser>> getUsersByRoleId(Long roleId) {
        //查询角色下的用户ID列表
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, roleId)
        );
        if (userRoles.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        //获取用户ID列表
        List<Long> userIds = userRoles.stream()
                .map(SysUserRole::getUserId)
                .collect(Collectors.toList());

        //查询用户信息
        List<SysUser> users = sysUserMapper.selectBatchIds(userIds);
        return Result.success(users);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> assignUsersToRole(Long roleId, List<Long> userIds) {
        //校验角色是否存在
        SysRole existRole = sysRoleMapper.selectById(roleId);
        if (existRole == null) {
            return Result.fail("角色不存在");
        }

        if (userIds == null || userIds.isEmpty()) {
            return Result.fail("用户ID列表不能为空");
        }

        //过滤已存在的用户角色关联，避免重复添加
        List<SysUserRole> existUserRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getRoleId, roleId)
                        .in(SysUserRole::getUserId, userIds)
        );
        List<Long> existUserIds = existUserRoles.stream()
                .map(SysUserRole::getUserId)
                .collect(Collectors.toList());

        //添加新的用户角色关联
        List<SysUserRole> newUserRoles = userIds.stream()
                .filter(userId -> !existUserIds.contains(userId))
                .map(userId -> {
                    SysUserRole userRole = new SysUserRole();
                    userRole.setUserId(userId);
                    userRole.setRoleId(roleId);
                    userRole.setCreateTime(LocalDateTime.now());
                    return userRole;
                })
                .collect(Collectors.toList());

        if (!newUserRoles.isEmpty()) {
            for (SysUserRole userRole : newUserRoles) {
                sysUserRoleMapper.insert(userRole);
            }
        }

        log.info("为角色分配用户成功, roleId: {}, 添加数量: {}", roleId, newUserRoles.size());
        return Result.success("分配用户成功", null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> removeUserFromRole(Long roleId, Long userId) {
        //删除用户角色关联
        sysUserRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getRoleId, roleId)
                        .eq(SysUserRole::getUserId, userId)
        );

        log.info("从角色移除用户成功, roleId: {}, userId: {}", roleId, userId);
        return Result.success("移除用户成功", null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> removeUsersFromRole(Long roleId, List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Result.fail("用户ID列表不能为空");
        }

        //批量删除用户角色关联
        sysUserRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getRoleId, roleId)
                        .in(SysUserRole::getUserId, userIds)
        );

        log.info("批量从角色移除用户成功, roleId: {}, 数量: {}", roleId, userIds.size());
        return Result.success("批量移除用户成功", null);
    }
}
