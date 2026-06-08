package com.example.evolutionary_ai_model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.evolutionary_ai_model.entity.AiProviderConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用法：AI供应商配置Mapper接口，负责ai_provider_config表的数据访问操作。
 * 位于数据访问层，继承MyBatis-Plus的BaseMapper，提供基础的CRUD操作。
 */
@Mapper
public interface AiProviderConfigMapper extends BaseMapper<AiProviderConfig> {
}