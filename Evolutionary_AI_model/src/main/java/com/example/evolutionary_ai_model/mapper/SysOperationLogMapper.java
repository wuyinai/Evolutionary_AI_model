package com.example.evolutionary_ai_model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.evolutionary_ai_model.entity.SysOperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用法：操作日志 Mapper，提供 sys_operation_log 表的基础数据访问操作。
 */
@Mapper
public interface SysOperationLogMapper extends BaseMapper<SysOperationLog> {
}
