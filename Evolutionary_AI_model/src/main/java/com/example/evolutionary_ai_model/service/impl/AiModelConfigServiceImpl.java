package com.example.evolutionary_ai_model.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.evolutionary_ai_model.entity.dto.AiModelConfigAddDTO;
import com.example.evolutionary_ai_model.entity.dto.AiModelConfigUpdateDTO;
import com.example.evolutionary_ai_model.entity.AiModelConfig;
import com.example.evolutionary_ai_model.entity.AiProviderConfig;
import com.example.evolutionary_ai_model.mapper.AiModelConfigMapper;
import com.example.evolutionary_ai_model.service.AiModelConfigService;
import com.example.evolutionary_ai_model.service.AiProviderConfigService;
import com.example.evolutionary_ai_model.service.factory.ProviderChatModelFactory;
import com.example.evolutionary_ai_model.service.factory.ProviderEmbeddingModelFactory;
import com.example.evolutionary_ai_model.entity.vo.AiModelConfigVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用法：AI模型配置服务实现类，负责模型配置的增删改查等业务逻辑。
 * 依赖AiModelConfigMapper进行数据持久化，依赖AiProviderConfigService获取供应商配置信息。
 * 只管理推理参数（温度、Token上限等），连接信息由关联的供应商配置管理。
 */
@Service
public class AiModelConfigServiceImpl implements AiModelConfigService {

    private static final Logger logger = LoggerFactory.getLogger(AiModelConfigServiceImpl.class);

    private final AiModelConfigMapper configMapper;
    private final AiProviderConfigService providerConfigService;
    private final ProviderChatModelFactory chatModelFactory;
    private final ProviderEmbeddingModelFactory embeddingModelFactory;

    public AiModelConfigServiceImpl(AiModelConfigMapper configMapper,
                                    AiProviderConfigService providerConfigService,
                                    ProviderChatModelFactory chatModelFactory,
                                    ProviderEmbeddingModelFactory embeddingModelFactory) {
        this.configMapper = configMapper;
        this.providerConfigService = providerConfigService;
        this.chatModelFactory = chatModelFactory;
        this.embeddingModelFactory = embeddingModelFactory;
    }

    @Override
    public List<AiModelConfigVO> listByUserId(Long userId) {
        logger.info("获取用户模型配置列表，用户ID: {}", userId);

        LambdaQueryWrapper<AiModelConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiModelConfig::getUserId, userId)
                .eq(AiModelConfig::getDelFlag, 0)
                .orderByDesc(AiModelConfig::getIsDefault)
                .orderByDesc(AiModelConfig::getCreateTime);

        List<AiModelConfig> configs = configMapper.selectList(wrapper);

        return configs.stream().map(config -> {
            AiModelConfigVO vo = new AiModelConfigVO();
            BeanUtils.copyProperties(config, vo);

            // 获取供应商配置名称
            if (config.getProviderConfigId() != null) {
                AiProviderConfig providerConfig = providerConfigService.getConfigById(config.getProviderConfigId());
                if (providerConfig != null) {
                    vo.setProviderName(providerConfig.getConfigName());
                }
            }

            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<AiModelConfigVO> listByUserIdAndType(Long userId, String modelType) {
        logger.info("获取用户指定类型模型配置列表，用户ID: {}, 模型类型: {}", userId, modelType);

        LambdaQueryWrapper<AiModelConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiModelConfig::getUserId, userId)
                .eq(AiModelConfig::getModelType, modelType)
                .eq(AiModelConfig::getDelFlag, 0)
                .orderByDesc(AiModelConfig::getIsDefault)
                .orderByDesc(AiModelConfig::getCreateTime);

        List<AiModelConfig> configs = configMapper.selectList(wrapper);

        return configs.stream().map(config -> {
            AiModelConfigVO vo = new AiModelConfigVO();
            BeanUtils.copyProperties(config, vo);

            // 获取供应商配置名称
            if (config.getProviderConfigId() != null) {
                AiProviderConfig providerConfig = providerConfigService.getConfigById(config.getProviderConfigId());
                if (providerConfig != null) {
                    vo.setProviderName(providerConfig.getConfigName());
                }
            }

            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addConfig(Long userId, AiModelConfigAddDTO dto) {
        logger.info("添加模型配置，用户ID: {}, 供应商配置ID: {}, 模型名称: {}", userId, dto.getProviderConfigId(), dto.getModelName());

        // 验证供应商配置是否存在
        AiProviderConfig providerConfig = providerConfigService.getConfigById(dto.getProviderConfigId());
        if (providerConfig == null) {
            logger.warn("供应商配置不存在，配置ID: {}", dto.getProviderConfigId());
            throw new IllegalArgumentException("供应商配置不存在");
        }

        // 如果设置为默认模型，先取消其他默认模型
        if (dto.getIsDefault() != null && dto.getIsDefault() == 1) {
            cancelOtherDefault(userId);
        }

        // 创建配置实体
        AiModelConfig config = new AiModelConfig();
        BeanUtils.copyProperties(dto, config);

        config.setId(IdUtil.getSnowflakeNextId());
        config.setUserId(userId);

        // 设置默认值
        if (config.getModelType() == null || config.getModelType().isEmpty()) {
            config.setModelType("CHAT"); // 默认为对话模型
        }
        if (config.getTemperature() == null) {
            config.setTemperature(new BigDecimal("0.70"));
        }
        if (config.getMaxTokens() == null) {
            config.setMaxTokens(4096);
        }
        if (config.getIsDefault() == null) {
            config.setIsDefault(0);
        }
        if (config.getIsStreamingEnabled() == null) {
            config.setIsStreamingEnabled(1);
        }
        if (config.getStatus() == null) {
            config.setStatus(1);
        }
        config.setUsedCount(0L);
        config.setUsedTokens(0L);
        config.setDelFlag(0);

        configMapper.insert(config);
        logger.info("模型配置添加成功，配置ID: {}", config.getId());

        return config.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(Long userId, AiModelConfigUpdateDTO dto) {
        logger.info("更新模型配置，用户ID: {}, 配置ID: {}", userId, dto.getId());

        // 验证配置是否存在且属于该用户
        AiModelConfig existing = configMapper.selectById(dto.getId());
        if (existing == null || existing.getUserId() != userId) {
            logger.warn("配置不存在或不属于该用户，配置ID: {}, 用户ID: {}", dto.getId(), userId);
            throw new IllegalArgumentException("配置不存在或无权限修改");
        }

        // 如果设置为默认模型，先取消其他默认模型
        if (dto.getIsDefault() != null && dto.getIsDefault() == 1) {
            cancelOtherDefault(userId);
        }

        // 更新配置
        AiModelConfig config = new AiModelConfig();
        BeanUtils.copyProperties(dto, config);

        configMapper.updateById(config);
        logger.info("模型配置更新成功，配置ID: {}", dto.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfig(Long userId, Long configId) {
        logger.info("删除模型配置，用户ID: {}, 配置ID: {}", userId, configId);

        // 验证配置是否存在且属于该用户
        AiModelConfig existing = configMapper.selectById(configId);
        if (existing == null || !Objects.equals(existing.getUserId(), userId)) {
            logger.warn("配置不存在或不属于该用户，配置ID: {}, 用户ID: {}", configId, userId);
            throw new IllegalArgumentException("配置不存在或无权限删除");
        }

        // 逻辑删除
        LambdaUpdateWrapper<AiModelConfig> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AiModelConfig::getId, configId)
                .set(AiModelConfig::getDelFlag, 1);

        configMapper.update(wrapper);
        logger.info("模型配置删除成功，配置ID: {}", configId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long userId, Long configId) {
        logger.info("设置默认模型，用户ID: {}, 配置ID: {}", userId, configId);

        // 验证配置是否存在且属于该用户
        AiModelConfig existing = configMapper.selectById(configId);
        if (existing == null || !Objects.equals(existing.getUserId(), userId)) {
            logger.warn("配置不存在或不属于该用户，配置ID: {}, 用户ID: {}", configId, userId);
            throw new IllegalArgumentException("配置不存在或无权限设置");
        }

        // 先取消其他默认模型
        cancelOtherDefault(userId);

        // 设置当前配置为默认
        LambdaUpdateWrapper<AiModelConfig> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AiModelConfig::getId, configId)
                .set(AiModelConfig::getIsDefault, 1);

        configMapper.update(wrapper);
        logger.info("默认模型设置成功，配置ID: {}", configId);
    }

    @Override
    public AiModelConfig getConfigById(Long configId) {
        logger.debug("获取模型配置详情，配置ID: {}", configId);

        AiModelConfig config = configMapper.selectById(configId);
        if (config == null || config.getDelFlag() == 1) {
            logger.warn("配置不存在或已删除，配置ID: {}", configId);
            return null;
        }

        return config;
    }

    @Override
    public AiModelConfig getDefaultConfig(Long userId) {
        logger.info("获取用户默认模型配置，用户ID: {}", userId);

        LambdaQueryWrapper<AiModelConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiModelConfig::getUserId, userId)
                .eq(AiModelConfig::getIsDefault, 1)
                .eq(AiModelConfig::getDelFlag, 0)
                .eq(AiModelConfig::getStatus, 1);

        AiModelConfig config = configMapper.selectOne(wrapper);

        if (config == null) {
            // 如果没有默认模型，获取第一个可用的模型
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiModelConfig::getUserId, userId)
                    .eq(AiModelConfig::getDelFlag, 0)
                    .eq(AiModelConfig::getStatus, 1)
                    .orderByDesc(AiModelConfig::getCreateTime)
                    .last("LIMIT 1");

            config = configMapper.selectOne(wrapper);
            logger.info("用户无默认模型，使用第一个可用模型，用户ID: {}", userId);
        }

        return config;
    }

    @Override
    public AiModelConfig getDefaultConfigByType(Long userId, String modelType) {
        logger.info("获取用户指定类型默认模型配置，用户ID: {}, 模型类型: {}", userId, modelType);

        LambdaQueryWrapper<AiModelConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiModelConfig::getUserId, userId)
                .eq(AiModelConfig::getModelType, modelType)
                .eq(AiModelConfig::getIsDefault, 1)
                .eq(AiModelConfig::getDelFlag, 0)
                .eq(AiModelConfig::getStatus, 1);

        AiModelConfig config = configMapper.selectOne(wrapper);

        if (config == null) {
            // 如果没有指定类型的默认模型，获取该类型的第一个可用模型
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiModelConfig::getUserId, userId)
                    .eq(AiModelConfig::getModelType, modelType)
                    .eq(AiModelConfig::getDelFlag, 0)
                    .eq(AiModelConfig::getStatus, 1)
                    .orderByDesc(AiModelConfig::getCreateTime)
                    .last("LIMIT 1");

            config = configMapper.selectOne(wrapper);
            logger.info("用户无指定类型默认模型，使用该类型第一个可用模型，用户ID: {}, 模型类型: {}", userId, modelType);
        }

        return config;
    }

    @Override
    public String testConnection(Long configId) {
        logger.info("测试模型连接，配置ID: {}", configId);

        AiModelConfig config = getConfigById(configId);
        if (config == null) {
            logger.warn("配置不存在，配置ID: {}", configId);
            throw new IllegalArgumentException("配置不存在");
        }

        // 验证是否关联了供应商配置
        if (config.getProviderConfigId() == null) {
            logger.warn("模型配置未关联供应商配置，配置ID: {}", configId);
            throw new IllegalArgumentException("模型配置未关联供应商配置");
        }

        // 获取供应商配置
        AiProviderConfig providerConfig = providerConfigService.getConfigById(config.getProviderConfigId());
        if (providerConfig == null) {
            logger.warn("供应商配置不存在，配置ID: {}", config.getProviderConfigId());
            throw new IllegalArgumentException("供应商配置不存在");
        }

        try {
            // 根据模型类型选择不同的测试方式
            String modelType = config.getModelType() != null ? config.getModelType() : "CHAT";

            if ("EMBEDDING".equals(modelType)) {
                // 测试向量模型连接
                return testEmbeddingConnection(providerConfig, config);
            } else {
                // 测试对话模型连接
                return testChatConnection(providerConfig, config);
            }

        } catch (Exception e) {
            logger.error("模型连接测试失败，配置ID: {}", configId, e);
            return "连接失败: " + e.getMessage();
        }
    }

    /**
     * 测试对话模型连接
     */
    private String testChatConnection(AiProviderConfig providerConfig, AiModelConfig config) {
        logger.info("测试对话模型连接，模型: {}", config.getModelName());

        // 使用工厂创建ChatClient
        ChatClient chatClient = chatModelFactory.getOrCreateChatClient(providerConfig, config);

        // 发送测试消息
        String response = chatClient.prompt()
                .user("Hello, this is a connection test. Please respond with 'OK'.")
                .call()
                .content();

        logger.info("对话模型连接测试成功，模型: {}, 响应: {}", config.getModelName(), response);
        return "连接成功，模型响应: " + response;
    }

    /**
     * 测试向量模型连接
     */
    private String testEmbeddingConnection(AiProviderConfig providerConfig, AiModelConfig config) {
        logger.info("测试向量模型连接，模型: {}", config.getModelName());

        // 使用工厂创建EmbeddingModel
        EmbeddingModel embeddingModel = embeddingModelFactory.getOrCreateEmbeddingModel(providerConfig, config);

        // 发送测试文本进行向量化
        String testText = "Hello, this is a connection test.";
        float[] embedding = embeddingModel.embed(testText);

        logger.info("向量模型连接测试成功，模型: {}, 向量维度: {}", config.getModelName(), embedding.length);
        return String.format("连接成功，向量维度: %d", embedding.length);
    }

    /**
     * 取消用户的其他默认模型
     * @param userId 用户ID
     */
    private void cancelOtherDefault(Long userId) {
        LambdaUpdateWrapper<AiModelConfig> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AiModelConfig::getUserId, userId)
                .eq(AiModelConfig::getIsDefault, 1)
                .set(AiModelConfig::getIsDefault, 0);

        configMapper.update(wrapper);
    }
}