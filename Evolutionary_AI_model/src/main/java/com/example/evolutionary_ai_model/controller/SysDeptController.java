package com.example.evolutionary_ai_model.controller;

import com.example.evolutionary_ai_model.annotation.OperationLog;
import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.entity.dto.DeptAddDTO;
import com.example.evolutionary_ai_model.entity.dto.DeptUpdateDTO;
import com.example.evolutionary_ai_model.entity.SysDept;
import com.example.evolutionary_ai_model.service.SysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用法：部门管理控制器，提供部门的增删改查REST接口，需要相应权限才能访问
 */
@RestController
@RequestMapping("/system/dept")
@RequiredArgsConstructor
public class SysDeptController {

    //部门管理服务，处理部门增删改查业务逻辑
    private final SysDeptService sysDeptService;

    /**
     * 添加部门
     */
    @PostMapping
    //需要 sys:dept:add 权限才能访问
    @PreAuthorize("hasAuthority('sys:dept:add')")
    public Result<Void> addDept(@RequestBody @Validated DeptAddDTO deptAddDTO) {
        return sysDeptService.addDept(deptAddDTO);
    }

    /**
     * 修改部门
     */
    @PutMapping
    //需要 sys:dept:edit 权限才能访问
    @PreAuthorize("hasAuthority('sys:dept:edit')")
    @OperationLog("修改部门")
    public Result<Void> updateDept(@RequestBody @Validated DeptUpdateDTO deptUpdateDTO) {
        return sysDeptService.updateDept(deptUpdateDTO);
    }

    /**
     * 删除部门
     */
    @DeleteMapping("/{deptId}")
    //需要 sys:dept:delete 权限才能访问
    @PreAuthorize("hasAuthority('sys:dept:delete')")
    @OperationLog("删除部门")
    public Result<Void> deleteDept(@PathVariable Long deptId) {
        return sysDeptService.deleteDept(deptId);
    }

    /**
     * 根据ID查询部门信息
     */
    @GetMapping("/{deptId}")
    //需要 sys:dept:list 权限才能访问
    @PreAuthorize("hasAuthority('sys:dept:list')")
    public Result<SysDept> getDeptById(@PathVariable Long deptId) {
        return sysDeptService.getDeptById(deptId);
    }
}
