package com.example.evolutionary_ai_model.service;

import com.example.evolutionary_ai_model.entity.dto.AiModelConfigAddDTO;
import com.example.evolutionary_ai_model.entity.dto.AiModelConfigUpdateDTO;
import com.example.evolutionary_ai_model.entity.AiModelConfig;
import com.example.evolutionary_ai_model.entity.vo.AiModelConfigVO;

import java.util.List;

/**
 * 用法：AI模型配置服务接口，定义模型配置相关的业务操作。
 * 位于业务逻辑层，负责模型配置的增删改查、默认模型设置等业务逻辑。
 */
public interface AiModelConfigService {

    /**
     * 获取用户的模型配置列表
     * @param userId 用户ID
     * @return 模型配置列表
     */
    List<AiModelConfigVO> listByUserId(Long userId);

    /**
     * 添加模型配置
     * @param userId 用户ID
     * @param dto 添加请求DTO
     * @return 新配置ID
     */
    Long addConfig(Long userId, AiModelConfigAddDTO dto);

    /**
     * 更新模型配置
     * @param userId 用户ID
     * @param dto 更新请求DTO
     */
    void updateConfig(Long userId, AiModelConfigUpdateDTO dto);

    /**
     * 删除模型配置
     * @param userId 用户ID
     * @param configId 配置ID
     */
    void deleteConfig(Long userId, Long configId);

    /**
     * 设置默认模型
     * @param userId 用户ID
     * @param configId 配置ID
     */
    void setDefault(Long userId, Long configId);

    /**
     * 获取模型配置详情（包含完整API密钥，用于内部调用）
     * @param configId 配置ID
     * @return 模型配置实体
     */
    AiModelConfig getConfigById(Long configId);

    /**
     * 获取用户的默认模型配置
     * @param userId 用户ID
     * @return 默认模型配置实体
     */
    AiModelConfig getDefaultConfig(Long userId);

    /**
     * 测试模型连接
     * @param configId 配置ID
     * @return 测试结果
     */
    String testConnection(Long configId);
}