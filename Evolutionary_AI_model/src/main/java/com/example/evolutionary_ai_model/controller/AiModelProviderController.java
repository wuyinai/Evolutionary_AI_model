package com.example.evolutionary_ai_model.controller;

import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.service.AiModelProviderService;
import com.example.evolutionary_ai_model.entity.vo.AiModelProviderVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用法：AI模型供应商Controller，负责接收前端供应商相关请求。
 * 位于表现层，只负责接收请求、调用业务层、返回响应。
 * 提供供应商列表查询功能，供用户添加模型配置时选择供应商。
 */
@RestController
@RequestMapping("/ai/provider")
public class AiModelProviderController {

    private static final Logger logger = LoggerFactory.getLogger(AiModelProviderController.class);

    private final AiModelProviderService providerService;

    public AiModelProviderController(AiModelProviderService providerService) {
        this.providerService = providerService;
    }

    /**
     * 获取所有启用的供应商列表
     * 请求地址: GET /ai/provider/list
     * 返回数据: 所有启用的供应商列表，用于用户添加模型配置时选择
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('ai:provider:list')")
    public Result<List<AiModelProviderVO>> list() {
        logger.info("获取供应商列表请求");

        try {
            List<AiModelProviderVO> list = providerService.listEnabled();
            logger.info("获取供应商列表成功，数量: {}", list.size());
            return Result.success(list);
        } catch (Exception e) {
            logger.error("获取供应商列表异常", e);
            return Result.fail("获取供应商列表失败");
        }
    }

    /**
     * 根据编码获取供应商详情
     * 请求地址: GET /ai/provider/{code}
     */
    @GetMapping("/{code}")
    @PreAuthorize("hasAuthority('ai:provider:list')")
    public Result<AiModelProviderVO> getByCode(@PathVariable String code) {
        logger.info("获取供应商详情请求，编码: {}", code);

        try {
            com.example.evolutionary_ai_model.entity.AiModelProvider provider = providerService.getByCode(code);
            if (provider == null) {
                logger.warn("供应商不存在，编码: {}", code);
                return Result.fail(404, "供应商不存在");
            }

            AiModelProviderVO vo = new AiModelProviderVO();
            org.springframework.beans.BeanUtils.copyProperties(provider, vo);

            logger.info("获取供应商详情成功，编码: {}", code);
            return Result.success(vo);
        } catch (Exception e) {
            logger.error("获取供应商详情异常", e);
            return Result.fail("获取供应商详情失败");
        }
    }
}