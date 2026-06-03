package com.example.evolutionary_ai_model.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.dto.DeptAddDTO;
import com.example.evolutionary_ai_model.dto.DeptUpdateDTO;
import com.example.evolutionary_ai_model.entity.SysDept;
import com.example.evolutionary_ai_model.mapper.SysDeptMapper;
import com.example.evolutionary_ai_model.service.SysDeptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用法：部门管理服务实现类，处理部门的增删改查业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl implements SysDeptService {

    private final SysDeptMapper sysDeptMapper;

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
}
