package com.example.evolutionary_ai_model.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.dto.RoleAddDTO;
import com.example.evolutionary_ai_model.dto.RoleUpdateDTO;
import com.example.evolutionary_ai_model.entity.SysRole;
import com.example.evolutionary_ai_model.mapper.SysRoleMapper;
import com.example.evolutionary_ai_model.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用法：角色管理服务实现类，处理角色的增删改查业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl implements SysRoleService {

    private final SysRoleMapper sysRoleMapper;

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
}
