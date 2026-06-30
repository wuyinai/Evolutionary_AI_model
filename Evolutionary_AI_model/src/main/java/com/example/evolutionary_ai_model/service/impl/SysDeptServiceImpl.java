package com.example.evolutionary_ai_model.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.entity.dto.DeptAddDTO;
import com.example.evolutionary_ai_model.entity.dto.DeptUpdateDTO;
import com.example.evolutionary_ai_model.entity.SysDept;
import com.example.evolutionary_ai_model.entity.SysUser;
import com.example.evolutionary_ai_model.entity.SysUserRole;
import com.example.evolutionary_ai_model.mapper.SysDeptMapper;
import com.example.evolutionary_ai_model.mapper.SysUserMapper;
import com.example.evolutionary_ai_model.mapper.SysUserRoleMapper;
import com.example.evolutionary_ai_model.service.SysDeptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用法：部门管理服务实现类，处理部门的增删改查业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl implements SysDeptService {

    private final SysDeptMapper sysDeptMapper;
    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> addDept(DeptAddDTO deptAddDTO) {
        //校验部门编码是否已存在
        if (deptAddDTO.getDeptCode() != null) {
            Long count = sysDeptMapper.selectCount(
                    new LambdaQueryWrapper<SysDept>().eq(SysDept::getDeptCode, deptAddDTO.getDeptCode())
            );
            if (count > 0) {
                return Result.fail("部门编码已存在");
            }
        }

        //构建部门实体
        SysDept sysDept = new SysDept();
        sysDept.setParentId(deptAddDTO.getParentId());
        sysDept.setDeptName(deptAddDTO.getDeptName());
        sysDept.setDeptCode(deptAddDTO.getDeptCode());
        sysDept.setSort(deptAddDTO.getSort() != null ? deptAddDTO.getSort() : 0);
        sysDept.setLeader(deptAddDTO.getLeader());
        sysDept.setLeaderId(deptAddDTO.getLeaderId());
        sysDept.setPhone(deptAddDTO.getPhone());
        sysDept.setEmail(deptAddDTO.getEmail());
        sysDept.setStatus(deptAddDTO.getStatus() != null ? deptAddDTO.getStatus() : 1);
        sysDept.setRemark(deptAddDTO.getRemark());

        //构建祖级列表（ancestors），用于快速查询所有子部门
        if (deptAddDTO.getParentId() == 0) {
            //顶级部门
            sysDept.setAncestors("0");
        } else {
            //子部门：查询父部门的祖级列表，拼接父部门ID
            SysDept parentDept = sysDeptMapper.selectById(deptAddDTO.getParentId());
            if (parentDept == null) {
                return Result.fail("父部门不存在");
            }
            sysDept.setAncestors(parentDept.getAncestors() + "," + deptAddDTO.getParentId());
        }

        //插入部门记录
        sysDeptMapper.insert(sysDept);

        //如果有负责人用户ID，将该用户添加到部门
        if (deptAddDTO.getLeaderId() != null) {
            LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(SysUser::getId, deptAddDTO.getLeaderId());
            updateWrapper.set(SysUser::getDeptId, sysDept.getId());
            sysUserMapper.update(null, updateWrapper);
            log.info("负责人 {} 已添加到部门 {}", deptAddDTO.getLeaderId(), sysDept.getId());
        }

        log.info("添加部门成功: {}", deptAddDTO.getDeptName());
        return Result.success("添加部门成功", null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> updateDept(DeptUpdateDTO deptUpdateDTO) {
        //校验部门是否存在
        SysDept existDept = sysDeptMapper.selectById(deptUpdateDTO.getId());
        if (existDept == null) {
            return Result.fail("部门不存在");
        }

        //如果修改了部门编码，校验新编码是否已被占用
        if (deptUpdateDTO.getDeptCode() != null
                && !deptUpdateDTO.getDeptCode().equals(existDept.getDeptCode())) {
            Long count = sysDeptMapper.selectCount(
                    new LambdaQueryWrapper<SysDept>().eq(SysDept::getDeptCode, deptUpdateDTO.getDeptCode())
            );
            if (count > 0) {
                return Result.fail("部门编码已存在");
            }
        }

        //不允许将父部门设置为自己或自己的子部门
        if (deptUpdateDTO.getParentId() != null) {
            if (deptUpdateDTO.getParentId().equals(deptUpdateDTO.getId())) {
                return Result.fail("父部门不能是自身");
            }
            //检查新父部门是否是当前部门的子部门
            SysDept newParent = sysDeptMapper.selectById(deptUpdateDTO.getParentId());
            if (newParent != null && newParent.getAncestors() != null
                    && newParent.getAncestors().contains(deptUpdateDTO.getId().toString())) {
                return Result.fail("父部门不能是自身的子部门");
            }
        }

        //构建更新实体
        SysDept sysDept = new SysDept();
        sysDept.setId(deptUpdateDTO.getId());
        sysDept.setParentId(deptUpdateDTO.getParentId());
        sysDept.setDeptName(deptUpdateDTO.getDeptName());
        sysDept.setDeptCode(deptUpdateDTO.getDeptCode());
        sysDept.setSort(deptUpdateDTO.getSort());
        sysDept.setLeader(deptUpdateDTO.getLeader());
        sysDept.setLeaderId(deptUpdateDTO.getLeaderId());
        sysDept.setPhone(deptUpdateDTO.getPhone());
        sysDept.setEmail(deptUpdateDTO.getEmail());
        sysDept.setStatus(deptUpdateDTO.getStatus());
        sysDept.setRemark(deptUpdateDTO.getRemark());

        //如果修改了父部门，需要重新计算祖级列表
        if (deptUpdateDTO.getParentId() != null && !deptUpdateDTO.getParentId().equals(existDept.getParentId())) {
            if (deptUpdateDTO.getParentId() == 0) {
                sysDept.setAncestors("0");
            } else {
                SysDept newParent = sysDeptMapper.selectById(deptUpdateDTO.getParentId());
                if (newParent != null) {
                    sysDept.setAncestors(newParent.getAncestors() + "," + deptUpdateDTO.getParentId());
                }
            }
        }

        //更新部门记录
        sysDeptMapper.updateById(sysDept);

        //如果修改了负责人用户ID，将该用户添加到部门
        if (deptUpdateDTO.getLeaderId() != null) {
            LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(SysUser::getId, deptUpdateDTO.getLeaderId());
            updateWrapper.set(SysUser::getDeptId, deptUpdateDTO.getId());
            sysUserMapper.update(null, updateWrapper);
            log.info("负责人 {} 已添加到部门 {}", deptUpdateDTO.getLeaderId(), deptUpdateDTO.getId());
        }

        log.info("修改部门成功, deptId: {}", deptUpdateDTO.getId());
        return Result.success("修改部门成功", null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteDept(Long deptId) {
        //校验部门是否存在
        SysDept existDept = sysDeptMapper.selectById(deptId);
        if (existDept == null) {
            return Result.fail("部门不存在");
        }

        //校验部门下是否存在子部门
        Long childCount = sysDeptMapper.selectCount(
                new LambdaQueryWrapper<SysDept>().eq(SysDept::getParentId, deptId)
        );
        if (childCount > 0) {
            return Result.fail("该部门下存在子部门，无法删除");
        }

        //逻辑删除部门
        sysDeptMapper.deleteById(deptId);

        log.info("删除部门成功, deptId: {}", deptId);
        return Result.success("删除部门成功", null);
    }

    @Override
    public Result<SysDept> getDeptById(Long deptId) {
        SysDept sysDept = sysDeptMapper.selectById(deptId);
        if (sysDept == null) {
            return Result.fail("部门不存在");
        }
        return Result.success(sysDept);
    }

    @Override
    public List<SysDept> listAllDepts() {
        //查询所有启用状态的部门，按排序字段升序排列
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDept::getStatus, 1);
        wrapper.orderByAsc(SysDept::getSort);
        return sysDeptMapper.selectList(wrapper);
    }

    @Override
    public Page<SysDept> listDeptsPage(Integer page, Integer size, String deptName, String deptCode, Integer status, Long parentId) {
        Page<SysDept> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();

        //部门名称模糊查询
        if (StringUtils.hasText(deptName)) {
            wrapper.like(SysDept::getDeptName, deptName);
        }

        //部门编码模糊查询
        if (StringUtils.hasText(deptCode)) {
            wrapper.like(SysDept::getDeptCode, deptCode);
        }

        //状态筛选
        if (status != null) {
            wrapper.eq(SysDept::getStatus, status);
        }

        //父部门筛选
        if (parentId != null) {
            wrapper.eq(SysDept::getParentId, parentId);
        }

        //按排序字段升序排列
        wrapper.orderByAsc(SysDept::getSort);
        wrapper.orderByAsc(SysDept::getId);

        return sysDeptMapper.selectPage(pageObj, wrapper);
    }

    @Override
    public List<SysDept> listDeptTree(String deptName, String deptCode, Integer status) {
        //查询所有部门，按排序字段升序排列
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();

        //部门名称模糊查询
        if (StringUtils.hasText(deptName)) {
            wrapper.like(SysDept::getDeptName, deptName);
        }

        //部门编码模糊查询
        if (StringUtils.hasText(deptCode)) {
            wrapper.like(SysDept::getDeptCode, deptCode);
        }

        //状态筛选
        if (status != null) {
            wrapper.eq(SysDept::getStatus, status);
        }

        wrapper.orderByAsc(SysDept::getSort);
        wrapper.orderByAsc(SysDept::getId);
        return sysDeptMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> batchAssignUsers(Long deptId, List<Long> userIds) {
        //校验部门是否存在
        SysDept dept = sysDeptMapper.selectById(deptId);
        if (dept == null) {
            return Result.fail("部门不存在");
        }

        if (userIds == null || userIds.isEmpty()) {
            return Result.fail("用户ID列表不能为空");
        }

        //批量更新用户的部门ID
        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(SysUser::getId, userIds);
        updateWrapper.set(SysUser::getDeptId, deptId);

        int updateCount = sysUserMapper.update(null, updateWrapper);

        log.info("批量关联用户到部门成功, deptId: {}, userIds: {}, count: {}", deptId, userIds, updateCount);
        return Result.success("成功关联" + updateCount + "个用户到部门", null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> batchAssignUsersByRoles(Long deptId, List<Long> roleIds) {
        //校验部门是否存在
        SysDept dept = sysDeptMapper.selectById(deptId);
        if (dept == null) {
            return Result.fail("部门不存在");
        }

        if (roleIds == null || roleIds.isEmpty()) {
            return Result.fail("角色ID列表不能为空");
        }

        //查询角色关联的用户ID
        LambdaQueryWrapper<SysUserRole> userRoleWrapper = new LambdaQueryWrapper<>();
        userRoleWrapper.in(SysUserRole::getRoleId, roleIds);
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(userRoleWrapper);

        if (userRoles.isEmpty()) {
            return Result.fail("所选角色下没有用户");
        }

        //提取用户ID列表
        List<Long> userIds = userRoles.stream()
                .map(SysUserRole::getUserId)
                .distinct()
                .collect(Collectors.toList());

        //批量更新用户的部门ID
        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(SysUser::getId, userIds);
        updateWrapper.set(SysUser::getDeptId, deptId);

        int updateCount = sysUserMapper.update(null, updateWrapper);

        log.info("根据角色批量关联用户到部门成功, deptId: {}, roleIds: {}, userIds: {}, count: {}", deptId, roleIds, userIds, updateCount);
        return Result.success("成功关联" + updateCount + "个用户到部门", null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> removeUsersFromDept(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Result.fail("用户ID列表不能为空");
        }

        //批量移除用户的部门关联（设置为null）
        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(SysUser::getId, userIds);
        updateWrapper.set(SysUser::getDeptId, null);

        int updateCount = sysUserMapper.update(null, updateWrapper);

        log.info("移除用户与部门关联成功, userIds: {}, count: {}", userIds, updateCount);
        return Result.success("成功移除" + updateCount + "个用户", null);
    }

    @Override
    public List<Long> listUsersByDeptId(Long deptId) {
        //查询部门下的用户ID列表
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getDeptId, deptId);
        List<SysUser> users = sysUserMapper.selectList(wrapper);

        return users.stream()
                .map(SysUser::getId)
                .collect(Collectors.toList());
    }
}
