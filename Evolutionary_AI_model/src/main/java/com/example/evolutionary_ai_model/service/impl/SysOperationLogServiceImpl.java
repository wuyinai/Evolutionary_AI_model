package com.example.evolutionary_ai_model.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.entity.SysOperationLog;
import com.example.evolutionary_ai_model.mapper.SysOperationLogMapper;
import com.example.evolutionary_ai_model.service.SysOperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 用法：操作日志服务实现类，提供操作日志的异步保存和查询功能。
 * 异步保存避免影响主业务流程的性能。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysOperationLogServiceImpl implements SysOperationLogService {

    private final SysOperationLogMapper sysOperationLogMapper;

    @Override
    @Async
    public void saveLog(SysOperationLog operationLog) {
        try {
            sysOperationLogMapper.insert(operationLog);
            log.debug("操作日志记录成功, operation: {}, userId: {}",
                    operationLog.getOperation(), operationLog.getUserId());
        } catch (Exception e) {
            // 日志记录失败不影响主业务流程，仅打印警告
            log.warn("操作日志记录失败: {}", e.getMessage());
        }
    }

    @Override
    public Result<Page<SysOperationLog>> listLogs(Page<SysOperationLog> page) {
        QueryWrapper<SysOperationLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        Page<SysOperationLog> result = sysOperationLogMapper.selectPage(page, queryWrapper);
        return Result.success(result);
    }
}
