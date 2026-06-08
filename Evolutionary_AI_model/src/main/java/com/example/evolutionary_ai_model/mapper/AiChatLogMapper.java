package com.example.evolutionary_ai_model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.evolutionary_ai_model.entity.AiChatLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用法：AI调用日志Mapper接口，负责调用日志数据的持久化操作。
 * 位于数据访问层，继承MyBatis-Plus BaseMapper，提供基础CRUD操作。
 */
@Mapper
public interface AiChatLogMapper extends BaseMapper<AiChatLog> {
}