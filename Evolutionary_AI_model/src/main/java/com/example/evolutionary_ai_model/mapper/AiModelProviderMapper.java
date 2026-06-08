package com.example.evolutionary_ai_model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.evolutionary_ai_model.entity.AiModelProvider;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用法：AI模型供应商Mapper接口，负责供应商数据的持久化操作。
 * 位于数据访问层，继承MyBatis-Plus BaseMapper，提供基础CRUD操作。
 */
@Mapper
public interface AiModelProviderMapper extends BaseMapper<AiModelProvider> {
}