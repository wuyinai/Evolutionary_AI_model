package com.example.evolutionary_ai_model.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.evolutionary_ai_model.entity.AiModelProvider;
import com.example.evolutionary_ai_model.mapper.AiModelProviderMapper;
import com.example.evolutionary_ai_model.service.AiModelProviderService;
import com.example.evolutionary_ai_model.entity.vo.AiModelProviderVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用法：AI模型供应商服务实现类，负责供应商信息的查询等业务逻辑。
 * 依赖AiModelProviderMapper进行数据持久化操作。
 */
@Service
public class AiModelProviderServiceImpl implements AiModelProviderService {

    private static final Logger logger = LoggerFactory.getLogger(AiModelProviderServiceImpl.class);

    private final AiModelProviderMapper providerMapper;

    public AiModelProviderServiceImpl(AiModelProviderMapper providerMapper) {
        this.providerMapper = providerMapper;
    }

    @Override
    public List<AiModelProviderVO> listEnabled() {
        logger.info("获取所有启用的供应商列表");

        LambdaQueryWrapper<AiModelProvider> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiModelProvider::getStatus, 1)
                .orderByAsc(AiModelProvider::getSortOrder);

        List<AiModelProvider> providers = providerMapper.selectList(wrapper);

        return providers.stream().map(provider -> {
            AiModelProviderVO vo = new AiModelProviderVO();
            BeanUtils.copyProperties(provider, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public AiModelProvider getByCode(String providerCode) {
        logger.debug("根据编码获取供应商，编码: {}", providerCode);

        LambdaQueryWrapper<AiModelProvider> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiModelProvider::getProviderCode, providerCode)
                .eq(AiModelProvider::getStatus, 1);

        return providerMapper.selectOne(wrapper);
    }

    @Override
    public AiModelProvider getById(Long providerId) {
        logger.debug("根据ID获取供应商，ID: {}", providerId);
        return providerMapper.selectById(providerId);
    }
}