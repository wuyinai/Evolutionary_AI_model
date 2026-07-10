package com.example.evolutionary_ai_model.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

import java.util.List;

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
     * 分页查询部门列表（支持模糊查询和条件筛选）
     */
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('sys:dept:list')")
    public Result<Page<SysDept>> listDeptsPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String deptName,
            @RequestParam(required = false) String deptCode,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long parentId) {
        return Result.success(sysDeptService.listDeptsPage(page, size, deptName, deptCode, status, parentId));
    }

    /**
     * 查询所有部门列表（用于下拉选择）
     * 用于角色分配用户功能，不做权限限制
     */
    @GetMapping("/list")
    public Result<List<SysDept>> listDepts() {
        return Result.success(sysDeptService.listAllDepts());
    }

    /**
     * 查询部门树形结构（用于前端展示，支持按名称/编码/状态筛选）
     */
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('sys:dept:list')")
    public Result<List<SysDept>> listDeptTree(
            @RequestParam(required = false) String deptName,
            @RequestParam(required = false) String deptCode,
            @RequestParam(required = false) Integer status) {
        return Result.success(sysDeptService.listDeptTree(deptName, deptCode, status));
    }

    /**
     * 添加部门
     */
    @PostMapping
    @PreAuthorize("hasAuthority('sys:dept:add')")
    @OperationLog("添加部门")
    public Result<Void> addDept(@RequestBody @Validated DeptAddDTO deptAddDTO) {
        return sysDeptService.addDept(deptAddDTO);
    }

    /**
     * 修改部门
     */
    @PutMapping
    @PreAuthorize("hasAuthority('sys:dept:edit')")
    @OperationLog("修改部门")
    public Result<Void> updateDept(@RequestBody @Validated DeptUpdateDTO deptUpdateDTO) {
        return sysDeptService.updateDept(deptUpdateDTO);
    }

    /**
     * 删除部门
     */
    @DeleteMapping("/{deptId}")
    @PreAuthorize("hasAuthority('sys:dept:delete')")
    @OperationLog("删除部门")
    public Result<Void> deleteDept(@PathVariable Long deptId) {
        return sysDeptService.deleteDept(deptId);
    }

    /**
     * 根据ID查询部门信息
     */
    @GetMapping("/{deptId}")
    @PreAuthorize("hasAuthority('sys:dept:list')")
    public Result<SysDept> getDeptById(@PathVariable Long deptId) {
        return sysDeptService.getDeptById(deptId);
    }

    /**
     * 批量关联用户到部门
     */
    @PostMapping("/{deptId}/users")
    @PreAuthorize("hasAuthority('sys:dept:edit')")
    @OperationLog("关联用户到部门")
    public Result<Void> batchAssignUsers(
            @PathVariable Long deptId,
            @RequestBody List<Long> userIds) {
        return sysDeptService.batchAssignUsers(deptId, userIds);
    }

    /**
     * 根据角色批量关联用户到部门
     */
    @PostMapping("/{deptId}/users/byRoles")
    @PreAuthorize("hasAuthority('sys:dept:edit')")
    @OperationLog("根据角色关联用户到部门")
    public Result<Void> batchAssignUsersByRoles(
            @PathVariable Long deptId,
            @RequestBody List<Long> roleIds) {
        return sysDeptService.batchAssignUsersByRoles(deptId, roleIds);
    }

    /**
     * 移除用户与部门的关联
     */
    @DeleteMapping("/users")
    @PreAuthorize("hasAuthority('sys:dept:edit')")
    @OperationLog("移除用户与部门关联")
    public Result<Void> removeUsersFromDept(@RequestBody List<Long> userIds) {
        return sysDeptService.removeUsersFromDept(userIds);
    }

    /**
     * 查询部门下的用户列表
     */
    @GetMapping("/{deptId}/users")
    @PreAuthorize("hasAuthority('sys:dept:list')")
    public Result<List<Long>> listUsersByDeptId(@PathVariable Long deptId) {
        return Result.success(sysDeptService.listUsersByDeptId(deptId));
    }

    /**
     * 查询部门关联的知识库ID列表
     */
    @GetMapping("/{deptId}/knowledge-bases")
    @PreAuthorize("hasAuthority('sys:dept:list')")
    public Result<List<Long>> listKnowledgeBaseIdsByDeptId(@PathVariable Long deptId) {
        return Result.success(sysDeptService.listKnowledgeBaseIdsByDeptId(deptId));
    }
}
