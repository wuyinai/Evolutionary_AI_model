package com.example.evolutionary_ai_model.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.entity.SysOperationLog;

/**
 * 用法：操作日志服务接口，定义操作日志的查询和异步保存方法。
 */
public interface SysOperationLogService {

    /**
     * 异步保存操作日志
     *
     * @param operationLog 操作日志实体
     */
    void saveLog(SysOperationLog operationLog);

    /**
     * 分页查询操作日志
     *
     * @param page 分页参数
     * @return 分页结果
     */
    Result<Page<SysOperationLog>> listLogs(Page<SysOperationLog> page);
}
