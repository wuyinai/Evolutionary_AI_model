package com.example.evolutionary_ai_model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.evolutionary_ai_model.entity.AiConversationMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用法：AI会话消息Mapper接口，负责会话消息数据的持久化操作。
 * 位于数据访问层，继承MyBatis-Plus BaseMapper，提供基础CRUD操作。
 */
@Mapper
public interface AiConversationMessageMapper extends BaseMapper<AiConversationMessage> {
}