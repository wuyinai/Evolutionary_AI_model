package com.example.evolutionary_ai_model.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.entity.SysPermission;
import com.example.evolutionary_ai_model.entity.SysRolePermission;
import com.example.evolutionary_ai_model.entity.dto.PermissionAddDTO;
import com.example.evolutionary_ai_model.entity.dto.PermissionUpdateDTO;
import com.example.evolutionary_ai_model.mapper.SysPermissionMapper;
import com.example.evolutionary_ai_model.mapper.SysRolePermissionMapper;
import com.example.evolutionary_ai_model.security.LoginUserDetails;
import com.example.evolutionary_ai_model.service.SysPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单/权限管理服务实现类，处理菜单权限的增删改查业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl implements SysPermissionService {

    private final SysPermissionMapper sysPermissionMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;

    @Override
    public Result<List<SysPermission>> listAllPermissions() {
        LambdaQueryWrapper<SysPermission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(SysPermission::getParentId)
                .orderByAsc(SysPermission::getSort);
        List<SysPermission> list = sysPermissionMapper.selectList(queryWrapper);
        return Result.success(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> addPermission(PermissionAddDTO addDTO) {
        //校验权限编码是否已存在（如果填写了权限编码）
        if (addDTO.getPermissionCode() != null && !addDTO.getPermissionCode().isEmpty()) {
            Long count = sysPermissionMapper.selectCount(
                    new LambdaQueryWrapper<SysPermission>()
                            .eq(SysPermission::getPermissionCode, addDTO.getPermissionCode())
            );
            if (count > 0) {
                return Result.fail("权限编码已存在");
            }
        }

        SysPermission permission = new SysPermission();
        permission.setParentId(addDTO.getParentId());
        permission.setPermissionName(addDTO.getPermissionName());
        permission.setPermissionCode(addDTO.getPermissionCode());
        permission.setPermissionType(addDTO.getPermissionType());
        permission.setPath(addDTO.getPath());
        permission.setComponent(addDTO.getComponent());
        permission.setIcon(addDTO.getIcon());
        permission.setSort(addDTO.getSort() != null ? addDTO.getSort() : 0);
        permission.setVisible(addDTO.getVisible() != null ? addDTO.getVisible() : 1);
        permission.setStatus(addDTO.getStatus() != null ? addDTO.getStatus() : 1);
        permission.setRemark(addDTO.getRemark());

        sysPermissionMapper.insert(permission);

        log.info("添加菜单/权限成功: {}", addDTO.getPermissionName());
        return Result.success("添加成功", null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> updatePermission(PermissionUpdateDTO updateDTO) {
        SysPermission existPermission = sysPermissionMapper.selectById(updateDTO.getId());
        if (existPermission == null) {
            return Result.fail("菜单/权限不存在");
        }

        //如果修改了权限编码，校验新编码是否已被占用
        if (updateDTO.getPermissionCode() != null
                && !updateDTO.getPermissionCode().equals(existPermission.getPermissionCode())) {
            Long count = sysPermissionMapper.selectCount(
                    new LambdaQueryWrapper<SysPermission>()
                            .eq(SysPermission::getPermissionCode, updateDTO.getPermissionCode())
            );
            if (count > 0) {
                return Result.fail("权限编码已存在");
            }
        }

        SysPermission permission = new SysPermission();
        permission.setId(updateDTO.getId());
        permission.setParentId(updateDTO.getParentId());
        permission.setPermissionName(updateDTO.getPermissionName());
        permission.setPermissionCode(updateDTO.getPermissionCode());
        permission.setPermissionType(updateDTO.getPermissionType());
        permission.setPath(updateDTO.getPath());
        permission.setComponent(updateDTO.getComponent());
        permission.setIcon(updateDTO.getIcon());
        permission.setSort(updateDTO.getSort());
        permission.setVisible(updateDTO.getVisible());
        permission.setStatus(updateDTO.getStatus());
        permission.setRemark(updateDTO.getRemark());

        sysPermissionMapper.updateById(permission);

        log.info("修改菜单/权限成功, permissionId: {}", updateDTO.getId());
        return Result.success("修改成功", null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deletePermission(Long id) {
        SysPermission existPermission = sysPermissionMapper.selectById(id);
        if (existPermission == null) {
            return Result.fail("菜单/权限不存在");
        }

        //检查是否有子节点
        Long childCount = sysPermissionMapper.selectCount(
                new LambdaQueryWrapper<SysPermission>().eq(SysPermission::getParentId, id)
        );
        if (childCount > 0) {
            return Result.fail("存在子菜单/权限，请先删除子节点");
        }

        //逻辑删除
        sysPermissionMapper.deleteById(id);

        log.info("删除菜单/权限成功, permissionId: {}", id);
        return Result.success("删除成功", null);
    }

    @Override
    public Result<SysPermission> getPermissionById(Long id) {
        SysPermission permission = sysPermissionMapper.selectById(id);
        if (permission == null) {
            return Result.fail("菜单/权限不存在");
        }
        return Result.success(permission);
    }

    @Override
    public Result<List<SysPermission>> getUserMenuTree() {
        //获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUserDetails)) {
            return Result.fail("用户未登录");
        }
        LoginUserDetails loginUser = (LoginUserDetails) authentication.getPrincipal();
        Long userId = loginUser.getUserId();

        //查询用户有权限的权限列表
        List<SysPermission> allPermissions = sysPermissionMapper.selectPermissionsByUserId(userId);

        //只保留目录(1)和菜单(2)类型，过滤按钮(3)类型
        //同时只保留 visible = 1 的菜单
        List<SysPermission> menus = allPermissions.stream()
                .filter(p -> p.getPermissionType() != null && p.getPermissionType() <= 2)
                .filter(p -> p.getVisible() == null || p.getVisible() == 1)
                .collect(Collectors.toList());

        //按 parentId 和 sort 排序
        menus.sort((a, b) -> {
            int cmp = a.getParentId().compareTo(b.getParentId());
            if (cmp == 0) {
                int sortA = a.getSort() != null ? a.getSort() : 0;
                int sortB = b.getSort() != null ? b.getSort() : 0;
                return Integer.compare(sortA, sortB);
            }
            return cmp;
        });

        return Result.success(menus);
    }

    @Override
    public Result<List<Long>> getRolePermissionIds(Long roleId) {
        List<SysRolePermission> rolePermissions = sysRolePermissionMapper.selectList(
                new LambdaQueryWrapper<SysRolePermission>()
                        .eq(SysRolePermission::getRoleId, roleId)
        );
        List<Long> permissionIds = rolePermissions.stream()
                .map(SysRolePermission::getPermissionId)
                .collect(Collectors.toList());
        return Result.success(permissionIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> updateRolePermissions(Long roleId, List<Long> permissionIds) {
        //删除角色原有的所有权限关联
        sysRolePermissionMapper.delete(
                new LambdaQueryWrapper<SysRolePermission>()
                        .eq(SysRolePermission::getRoleId, roleId)
        );

        //插入新的权限关联
        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<SysRolePermission> newPermissions = permissionIds.stream()
                    .map(permissionId -> {
                        SysRolePermission rp = new SysRolePermission();
                        rp.setRoleId(roleId);
                        rp.setPermissionId(permissionId);
                        rp.setCreateTime(LocalDateTime.now());
                        return rp;
                    })
                    .collect(Collectors.toList());

            for (SysRolePermission rp : newPermissions) {
                sysRolePermissionMapper.insert(rp);
            }
        }

        log.info("更新角色权限成功, roleId: {}, 权限数量: {}", roleId, permissionIds != null ? permissionIds.size() : 0);
        return Result.success("更新权限成功", null);
    }
}
