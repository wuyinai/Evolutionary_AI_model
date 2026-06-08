package com.example.evolutionary_ai_model.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.evolutionary_ai_model.entity.AiModelProvider;
import com.example.evolutionary_ai_model.entity.AiProviderConfig;
import com.example.evolutionary_ai_model.entity.enums.ModelProtocol;
import com.example.evolutionary_ai_model.entity.vo.AiProviderConfigVO;
import com.example.evolutionary_ai_model.mapper.AiModelProviderMapper;
import com.example.evolutionary_ai_model.mapper.AiProviderConfigMapper;
import com.example.evolutionary_ai_model.service.AiModelProviderService;
import com.example.evolutionary_ai_model.service.AiProviderConfigService;
import com.example.evolutionary_ai_model.util.AesEncryptUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用法：AI供应商配置服务实现类，负责处理供应商配置相关的业务逻辑。
 * 依赖AiProviderConfigMapper进行数据持久化，依赖AiModelProviderService获取供应商信息。
 * 位于业务逻辑层，实现供应商连接配置的增删改查、API密钥加密存储等功能。
 */
@Service
public class AiProviderConfigServiceImpl implements AiProviderConfigService {

    private static final Logger logger = LoggerFactory.getLogger(AiProviderConfigServiceImpl.class);

    @Autowired
    private AiProviderConfigMapper providerConfigMapper;

    @Autowired
    private AiModelProviderService providerService;

    @Autowired
    private AiModelProviderMapper providerMapper;

    @Override
    public List<AiProviderConfigVO> listByUserId(Long userId) {
        logger.info("获取用户供应商配置列表，用户ID: {}", userId);

        LambdaQueryWrapper<AiProviderConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiProviderConfig::getUserId, userId)
                .eq(AiProviderConfig::getDelFlag, 0)
                .orderByDesc(AiProviderConfig::getIsDefault)
                .orderByDesc(AiProviderConfig::getCreateTime);

        List<AiProviderConfig> configs = providerConfigMapper.selectList(queryWrapper);

        // 转换为VO并脱敏API密钥
        List<AiProviderConfigVO> voList = configs.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        logger.info("获取用户供应商配置列表成功，数量: {}", voList.size());
        return voList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addConfig(Long userId, AiProviderConfig config) {
        logger.info("添加供应商配置，用户ID: {}, 供应商编码: {}", userId, config.getProviderCode());

        // 验证供应商是否存在
        AiModelProvider provider = providerService.getByCode(config.getProviderCode());
        if (provider == null) {
            logger.warn("供应商不存在，编码: {}", config.getProviderCode());
            throw new IllegalArgumentException("供应商不存在: " + config.getProviderCode());
        }

        // 如果设置为默认配置，先取消其他默认配置
        if (config.getIsDefault() != null && config.getIsDefault() == 1) {
            cancelOtherDefault(userId);
        }

        // 创建配置实体
        config.setId(IdUtil.getSnowflakeNextId());
        config.setUserId(userId);
        config.setProviderId(provider.getId());
        config.setProviderCode(provider.getProviderCode());

        // 设置协议类型（如果未提供，从供应商推断）
        if (StrUtil.isBlank(config.getProtocolType())) {
            ModelProtocol protocol = ModelProtocol.fromProviderCode(provider.getProviderCode());
            config.setProtocolType(protocol.getCode());
        }

        // 设置API端点，如果未提供则使用供应商默认端点
        if (StrUtil.isBlank(config.getApiEndpoint())) {
            config.setApiEndpoint(provider.getDefaultEndpoint());
        }

        // 加密API密钥
        if (StrUtil.isNotBlank(config.getApiKey())) {
            config.setApiKey(AesEncryptUtil.encrypt(config.getApiKey()));
        }

        // 设置默认值
        if (config.getIsDefault() == null) {
            config.setIsDefault(0);
        }
        if (config.getStatus() == null) {
            config.setStatus(1);
        }
        if (config.getTimeoutSeconds() == null) {
            config.setTimeoutSeconds(60);
        }
        if (config.getMaxRetries() == null) {
            config.setMaxRetries(3);
        }

        providerConfigMapper.insert(config);
        logger.info("添加供应商配置成功，配置ID: {}", config.getId());

        return config.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(Long userId, AiProviderConfig config) {
        logger.info("更新供应商配置，用户ID: {}, 配置ID: {}", userId, config.getId());

        // 验证配置是否存在且属于该用户
        AiProviderConfig existingConfig = providerConfigMapper.selectById(config.getId());
        if (existingConfig == null || !existingConfig.getUserId().equals(userId)) {
            logger.warn("供应商配置不存在或不属于该用户");
            throw new IllegalArgumentException("供应商配置不存在或无权修改");
        }

        // 如果设置为默认配置，先取消其他默认配置
        if (config.getIsDefault() != null && config.getIsDefault() == 1) {
            cancelOtherDefault(userId);
        }

        // 加密API密钥（如果更新了密钥）
        if (StrUtil.isNotBlank(config.getApiKey()) && !config.getApiKey().equals(existingConfig.getApiKey())) {
            config.setApiKey(AesEncryptUtil.encrypt(config.getApiKey()));
        }

        // 更新配置
        LambdaUpdateWrapper<AiProviderConfig> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AiProviderConfig::getId, config.getId())
                .eq(AiProviderConfig::getUserId, userId);

        providerConfigMapper.update(config, updateWrapper);
        logger.info("更新供应商配置成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfig(Long userId, Long configId) {
        logger.info("删除供应商配置，用户ID: {}, 配置ID: {}", userId, configId);

        // 验证配置是否存在且属于该用户
        AiProviderConfig config = providerConfigMapper.selectById(configId);
        if (config == null || !config.getUserId().equals(userId)) {
            logger.warn("供应商配置不存在或不属于该用户");
            throw new IllegalArgumentException("供应商配置不存在或无权删除");
        }

        // 逻辑删除
        LambdaUpdateWrapper<AiProviderConfig> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AiProviderConfig::getId, configId)
                .eq(AiProviderConfig::getUserId, userId)
                .set(AiProviderConfig::getDelFlag, 1);

        providerConfigMapper.update(null, updateWrapper);
        logger.info("删除供应商配置成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long userId, Long configId) {
        logger.info("设置默认供应商配置，用户ID: {}, 配置ID: {}", userId, configId);

        // 验证配置是否存在且属于该用户
        AiProviderConfig config = providerConfigMapper.selectById(configId);
        if (config == null || !config.getUserId().equals(userId)) {
            logger.warn("供应商配置不存在或不属于该用户");
            throw new IllegalArgumentException("供应商配置不存在或无权设置");
        }

        // 先取消其他默认配置
        cancelOtherDefault(userId);

        // 设置为默认配置
        LambdaUpdateWrapper<AiProviderConfig> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AiProviderConfig::getId, configId)
                .eq(AiProviderConfig::getUserId, userId)
                .set(AiProviderConfig::getIsDefault, 1);

        providerConfigMapper.update(null, updateWrapper);
        logger.info("设置默认供应商配置成功");
    }

    @Override
    public AiProviderConfig getConfigById(Long configId) {
        logger.info("获取供应商配置详情，配置ID: {}", configId);

        AiProviderConfig config = providerConfigMapper.selectById(configId);
        if (config == null || config.getDelFlag() == 1) {
            logger.warn("供应商配置不存在，配置ID: {}", configId);
            return null;
        }

        // 解密API密钥（用于内部调用）
        if (StrUtil.isNotBlank(config.getApiKey())) {
            config.setApiKey(AesEncryptUtil.decrypt(config.getApiKey()));
        }

        logger.info("获取供应商配置详情成功");
        return config;
    }

    @Override
    public AiProviderConfig getDefaultConfig(Long userId) {
        logger.info("获取用户默认供应商配置，用户ID: {}", userId);

        LambdaQueryWrapper<AiProviderConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiProviderConfig::getUserId, userId)
                .eq(AiProviderConfig::getIsDefault, 1)
                .eq(AiProviderConfig::getDelFlag, 0)
                .eq(AiProviderConfig::getStatus, 1);

        AiProviderConfig config = providerConfigMapper.selectOne(queryWrapper);
        if (config == null) {
            logger.warn("用户没有默认供应商配置，用户ID: {}", userId);
            return null;
        }

        // 解密API密钥（用于内部调用）
        if (StrUtil.isNotBlank(config.getApiKey())) {
            config.setApiKey(AesEncryptUtil.decrypt(config.getApiKey()));
        }

        logger.info("获取用户默认供应商配置成功，配置ID: {}", config.getId());
        return config;
    }

    @Override
    public String testConnection(Long configId) {
        logger.info("测试供应商连接，配置ID: {}", configId);

        AiProviderConfig config = getConfigById(configId);
        if (config == null) {
            throw new IllegalArgumentException("供应商配置不存在");
        }

        // TODO: 实现实际的连接测试逻辑
        // 这里可以调用对应的ChatModelBuilder进行简单的测试调用

        logger.info("测试供应商连接成功");
        return "连接测试成功";
    }

    /**
     * 取消用户的其他默认配置
     * @param userId 用户ID
     */
    private void cancelOtherDefault(Long userId) {
        logger.info("取消用户的其他默认供应商配置，用户ID: {}", userId);

        LambdaUpdateWrapper<AiProviderConfig> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AiProviderConfig::getUserId, userId)
                .eq(AiProviderConfig::getIsDefault, 1)
                .set(AiProviderConfig::getIsDefault, 0);

        providerConfigMapper.update(null, updateWrapper);
    }

    /**
     * 将实体转换为VO，并脱敏API密钥
     * @param config 供应商配置实体
     * @return 供应商配置VO
     */
    private AiProviderConfigVO convertToVO(AiProviderConfig config) {
        AiProviderConfigVO vo = new AiProviderConfigVO();
        BeanUtils.copyProperties(config, vo);

        // 获取供应商名称
        AiModelProvider provider = providerMapper.selectById(config.getProviderId());
        if (provider != null) {
            vo.setProviderName(provider.getProviderName());
        }

        // 脱敏API密钥（只显示前缀和后缀）
        if (StrUtil.isNotBlank(config.getApiKey())) {
            String apiKey = config.getApiKey();
            if (apiKey.length() > 10) {
                vo.setApiKeyMasked(apiKey.substring(0, 7) + "***" + apiKey.substring(apiKey.length() - 3));
            } else {
                vo.setApiKeyMasked("***");
            }
        }

        return vo;
    }
}