package com.example.evolutionary_ai_model.service;

import com.example.evolutionary_ai_model.entity.AiProviderConfig;
import com.example.evolutionary_ai_model.entity.vo.AiProviderConfigVO;

import java.util.List;

/**
 * 用法：AI供应商配置服务接口，定义供应商配置相关的业务操作。
 * 位于业务逻辑层，负责供应商连接配置的增删改查、默认配置设置等业务逻辑。
 */
public interface AiProviderConfigService {

    /**
     * 获取用户的供应商配置列表
     * @param userId 用户ID
     * @return 供应商配置列表
     */
    List<AiProviderConfigVO> listByUserId(Long userId);

    /**
     * 添加供应商配置
     * @param userId 用户ID
     * @param providerConfig 供应商配置实体
     * @return 新配置ID
     */
    Long addConfig(Long userId, AiProviderConfig providerConfig);

    /**
     * 更新供应商配置
     * @param userId 用户ID
     * @param providerConfig 供应商配置实体
     */
    void updateConfig(Long userId, AiProviderConfig providerConfig);

    /**
     * 删除供应商配置
     * @param userId 用户ID
     * @param configId 配置ID
     */
    void deleteConfig(Long userId, Long configId);

    /**
     * 设置默认配置
     * @param userId 用户ID
     * @param configId 配置ID
     */
    void setDefault(Long userId, Long configId);

    /**
     * 获取供应商配置详情（包含完整API密钥，用于内部调用）
     * @param configId 配置ID
     * @return 供应商配置实体
     */
    AiProviderConfig getConfigById(Long configId);

    /**
     * 获取用户的默认供应商配置
     * @param userId 用户ID
     * @return 默认供应商配置实体
     */
    AiProviderConfig getDefaultConfig(Long userId);

    /**
     * 测试供应商连接
     * @param configId 配置ID
     * @return 测试结果
     */
    String testConnection(Long configId);
}