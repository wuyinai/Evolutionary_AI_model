package com.example.evolutionary_ai_model.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.entity.SysApproval;
import com.example.evolutionary_ai_model.entity.SysDict;
import com.example.evolutionary_ai_model.mapper.SysApprovalMapper;
import com.example.evolutionary_ai_model.mapper.SysDictMapper;
import com.example.evolutionary_ai_model.service.SysApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用法：审批管理服务实现类，处理审批的查询业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysApprovalServiceImpl implements SysApprovalService {

    private final SysApprovalMapper sysApprovalMapper;
    private final SysDictMapper sysDictMapper;

    @Override
    public Result<Page<SysApproval>> listApprovals(int page, int size) {
        Page<SysApproval> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SysApproval> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(SysApproval::getCreateTime);
        Page<SysApproval> result = sysApprovalMapper.selectPage(pageParam, queryWrapper);
        log.info("查询审批列表，页码: {}, 大小: {}, 总数: {}", page, size, result.getTotal());
        return Result.success(result);
    }

    @Override
    public Result<Page<SysApproval>> listApprovalsByType(String approvalType, int page, int size) {
        Page<SysApproval> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SysApproval> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysApproval::getApprovalType, approvalType);
        queryWrapper.orderByDesc(SysApproval::getCreateTime);
        Page<SysApproval> result = sysApprovalMapper.selectPage(pageParam, queryWrapper);
        log.info("按审批类型查询，类型: {}, 总数: {}", approvalType, result.getTotal());
        return Result.success(result);
    }

    @Override
    public Result<Page<SysApproval>> listApprovalsByStatus(Integer approvalStatus, int page, int size) {
        Page<SysApproval> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SysApproval> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysApproval::getApprovalStatus, approvalStatus);
        queryWrapper.orderByDesc(SysApproval::getCreateTime);
        Page<SysApproval> result = sysApprovalMapper.selectPage(pageParam, queryWrapper);
        log.info("按审批状态查询，状态: {}, 总数: {}", approvalStatus, result.getTotal());
        return Result.success(result);
    }

    @Override
    public Result<Page<SysApproval>> listApprovalsByApplicant(Long applicantId, int page, int size) {
        Page<SysApproval> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SysApproval> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysApproval::getApplicantId, applicantId);
        queryWrapper.orderByDesc(SysApproval::getCreateTime);
        Page<SysApproval> result = sysApprovalMapper.selectPage(pageParam, queryWrapper);
        log.info("按申请人查询，申请人ID: {}, 总数: {}", applicantId, result.getTotal());
        return Result.success(result);
    }

    @Override
    public Result<SysApproval> getApprovalById(Long approvalId) {
        SysApproval approval = sysApprovalMapper.selectById(approvalId);
        if (approval == null) {
            log.warn("审批不存在，ID: {}", approvalId);
            return Result.fail("审批不存在");
        }
        log.info("查询审批详情，ID: {}", approvalId);
        return Result.success(approval);
    }

    @Override
    public Result<List<SysDict>> getApprovalTypes() {
        List<SysDict> approvalTypes = sysDictMapper.selectByDictType("approval_type");
        log.info("查询审批类型字典，数量: {}", approvalTypes.size());
        return Result.success(approvalTypes);
    }

    @Override
    public Result<List<SysDict>> getApprovalStatuses() {
        List<SysDict> approvalStatuses = sysDictMapper.selectByDictType("approval_status");
        log.info("查询审批状态字典，数量: {}", approvalStatuses.size());
        return Result.success(approvalStatuses);
    }
}