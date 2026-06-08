package com.example.evolutionary_ai_model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.evolutionary_ai_model.entity.AiModelConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用法：AI模型配置Mapper接口，负责模型配置数据的持久化操作。
 * 位于数据访问层，继承MyBatis-Plus BaseMapper，提供基础CRUD操作。
 */
@Mapper
public interface AiModelConfigMapper extends BaseMapper<AiModelConfig> {
}