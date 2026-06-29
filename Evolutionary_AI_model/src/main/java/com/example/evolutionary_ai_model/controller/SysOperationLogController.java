package com.example.evolutionary_ai_model.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.entity.SysOperationLog;
import com.example.evolutionary_ai_model.service.SysOperationLogService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用法：操作日志查询Controller，提供操作日志的分页查询、删除功能。
 * 需要 sys:log:list 权限才能访问。
 */
@RestController
@RequestMapping("/system/log")
@RequiredArgsConstructor
public class SysOperationLogController {

    private static final Logger logger = LoggerFactory.getLogger(SysOperationLogController.class);

    private final SysOperationLogService sysOperationLogService;

    /**
     * 分页查询操作日志
     * 请求地址: GET /system/log/list
     * 测试数据: ?page=1&size=10
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('sys:log:list')")
    public Result<Page<SysOperationLog>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("查询操作日志请求，page: {}, size: {}", page, size);

        try {
            Page<SysOperationLog> pageParam = new Page<>(page, size);
            return sysOperationLogService.listLogs(pageParam);
        } catch (Exception e) {
            logger.error("查询操作日志失败", e);
            return Result.fail("查询操作日志失败: " + e.getMessage());
        }
    }

    /**
     * 删除单条操作日志
     * 请求地址: DELETE /system/log/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:log:delete')")
    public Result<Void> deleteLog(@PathVariable Long id) {
        logger.info("删除操作日志请求，id: {}", id);

        try {
            sysOperationLogService.deleteLog(id);
            logger.info("操作日志删除成功，id: {}", id);
            return Result.success();
        } catch (Exception e) {
            logger.error("删除操作日志失败", e);
            return Result.fail("删除操作日志失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除操作日志
     * 请求地址: DELETE /system/log/batch
     * 测试数据: [1, 2, 3]
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('sys:log:delete')")
    public Result<Void> deleteLogs(@RequestBody List<Long> ids) {
        logger.info("批量删除操作日志请求，数量: {}", ids.size());

        try {
            sysOperationLogService.deleteLogs(ids);
            logger.info("批量删除操作日志成功，数量: {}", ids.size());
            return Result.success();
        } catch (Exception e) {
            logger.error("批量删除操作日志失败", e);
            return Result.fail("批量删除操作日志失败: " + e.getMessage());
        }
    }

    /**
     * 清空所有操作日志
     * 请求地址: DELETE /system/log/clear
     */
    @DeleteMapping("/clear")
    @PreAuthorize("hasAuthority('sys:log:clear')")
    public Result<Void> clearLogs() {
        logger.info("清空操作日志请求");

        try {
            sysOperationLogService.clearLogs();
            logger.info("清空操作日志成功");
            return Result.success();
        } catch (Exception e) {
            logger.error("清空操作日志失败", e);
            return Result.fail("清空操作日志失败: " + e.getMessage());
        }
    }

    /**
     * 获取操作日志详情
     * 请求地址: GET /system/log/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:log:list')")
    public Result<SysOperationLog> getLogDetail(@PathVariable Long id) {
        logger.info("获取操作日志详情请求，id: {}", id);

        try {
            SysOperationLog log = sysOperationLogService.getLogById(id);
            if (log == null) {
                return Result.fail("操作日志不存在");
            }
            return Result.success(log);
        } catch (Exception e) {
            logger.error("获取操作日志详情失败", e);
            return Result.fail("获取操作日志详情失败: " + e.getMessage());
        }
    }
}
