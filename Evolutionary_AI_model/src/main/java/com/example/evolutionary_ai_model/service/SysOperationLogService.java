package com.example.evolutionary_ai_model.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.entity.SysOperationLog;

import java.util.List;

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

    /**
     * 删除单条操作日志
     *
     * @param id 日志ID
     */
    void deleteLog(Long id);

    /**
     * 批量删除操作日志
     *
     * @param ids 日志ID列表
     */
    void deleteLogs(List<Long> ids);

    /**
     * 清空所有操作日志
     */
    void clearLogs();

    /**
     * 根据ID获取操作日志详情
     *
     * @param id 日志ID
     * @return 操作日志实体
     */
    SysOperationLog getLogById(Long id);
}
