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

/**
 * 用法：操作日志查询Controller，提供操作日志的分页查询功能。
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
}
