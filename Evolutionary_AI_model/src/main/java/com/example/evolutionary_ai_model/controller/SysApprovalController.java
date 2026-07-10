package com.example.evolutionary_ai_model.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.entity.SysApproval;
import com.example.evolutionary_ai_model.entity.SysDict;
import com.example.evolutionary_ai_model.service.SysApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用法：审批管理控制器，提供审批查询的REST接口，需要相应权限才能访问
 */
@RestController
@RequestMapping("/system/approval")
@RequiredArgsConstructor
public class SysApprovalController {

    // 审批管理服务，处理审批查询业务逻辑
    private final SysApprovalService sysApprovalService;

    /**
     * 分页查询审批列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('sys:approval:list')")
    public Result<Page<SysApproval>> listApprovals(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return sysApprovalService.listApprovals(page, size);
    }

    /**
     * 根据审批类型分页查询审批列表
     */
    @GetMapping("/list/type/{approvalType}")
    @PreAuthorize("hasAuthority('sys:approval:list')")
    public Result<Page<SysApproval>> listApprovalsByType(
            @PathVariable String approvalType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return sysApprovalService.listApprovalsByType(approvalType, page, size);
    }

    /**
     * 根据审批状态分页查询审批列表
     */
    @GetMapping("/list/status/{approvalStatus}")
    @PreAuthorize("hasAuthority('sys:approval:list')")
    public Result<Page<SysApproval>> listApprovalsByStatus(
            @PathVariable Integer approvalStatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return sysApprovalService.listApprovalsByStatus(approvalStatus, page, size);
    }

    /**
     * 根据申请人ID分页查询审批列表
     */
    @GetMapping("/list/applicant/{applicantId}")
    @PreAuthorize("hasAuthority('sys:approval:list')")
    public Result<Page<SysApproval>> listApprovalsByApplicant(
            @PathVariable Long applicantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return sysApprovalService.listApprovalsByApplicant(applicantId, page, size);
    }

    /**
     * 根据ID查询审批详情
     */
    @GetMapping("/{approvalId}")
    @PreAuthorize("hasAuthority('sys:approval:list')")
    public Result<SysApproval> getApprovalById(@PathVariable Long approvalId) {
        return sysApprovalService.getApprovalById(approvalId);
    }

    /**
     * 查询审批类型字典列表
     */
    @GetMapping("/dict/types")
    @PreAuthorize("hasAuthority('sys:approval:list')")
    public Result<List<SysDict>> getApprovalTypes() {
        return sysApprovalService.getApprovalTypes();
    }

    /**
     * 查询审批状态字典列表
     */
    @GetMapping("/dict/statuses")
    @PreAuthorize("hasAuthority('sys:approval:list')")
    public Result<List<SysDict>> getApprovalStatuses() {
        return sysApprovalService.getApprovalStatuses();
    }
}