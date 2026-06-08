package com.example.evolutionary_ai_model.service;

import com.example.evolutionary_ai_model.entity.AiModelProvider;
import com.example.evolutionary_ai_model.entity.vo.AiModelProviderVO;

import java.util.List;

/**
 * 用法：AI模型供应商服务接口，定义供应商相关的业务操作。
 * 位于业务逻辑层，负责供应商信息的查询等业务逻辑。
 */
public interface AiModelProviderService {

    /**
     * 获取所有启用的供应商列表
     * @return 供应商列表
     */
    List<AiModelProviderVO> listEnabled();

    /**
     * 根据编码获取供应商信息
     * @param providerCode 供应商编码
     * @return 供应商实体
     */
    AiModelProvider getByCode(String providerCode);

    /**
     * 根据ID获取供应商信息
     * @param providerId 供应商ID
     * @return 供应商实体
     */
    AiModelProvider getById(Long providerId);
}