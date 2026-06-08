package com.example.evolutionary_ai_model.controller;

import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.entity.dto.AiModelConfigAddDTO;
import com.example.evolutionary_ai_model.entity.dto.AiModelConfigUpdateDTO;
import com.example.evolutionary_ai_model.service.AiModelConfigService;
import com.example.evolutionary_ai_model.entity.vo.AiModelConfigVO;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用法：AI模型配置Controller，负责接收前端模型配置管理请求。
 * 位于表现层，只负责接收请求、参数校验、调用业务层、返回响应。
 * 提供模型配置的增删改查、设置默认模型、测试连接等功能。
 */
@RestController
@RequestMapping("/ai/config")
public class AiModelConfigController {

    private static final Logger logger = LoggerFactory.getLogger(AiModelConfigController.class);

    private final AiModelConfigService configService;

    public AiModelConfigController(AiModelConfigService configService) {
        this.configService = configService;
    }

    /**
     * 获取用户的模型配置列表
     * 请求地址: GET /ai/config/list
     * 返回数据: 用户的所有模型配置列表
     */
    @GetMapping("/list")
    public Result<List<AiModelConfigVO>> list(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("获取模型配置列表请求");

        try {
            Long userId = getUserId(userDetails);
            List<AiModelConfigVO> list = configService.listByUserId(userId);
            logger.info("获取模型配置列表成功，数量: {}", list.size());
            return Result.success(list);
        } catch (Exception e) {
            logger.error("获取模型配置列表异常", e);
            return Result.fail("获取模型配置列表失败");
        }
    }

    /**
     * 添加模型配置
     * 请求地址: POST /ai/config/add
     * 测试数据示例:
     * {
     *   "configName": "我的DeepSeek配置",
     *   "providerCode": "DEEPSEEK",
     *   "modelName": "deepseek-chat",
     *   "apiKey": "sk-xxxxxxxxxxxxx",
     *   "temperature": 0.7
     * }
     */
    @PostMapping("/add")
    public Result<Long> add(@AuthenticationPrincipal UserDetails userDetails,
                            @Valid @RequestBody AiModelConfigAddDTO dto) {
        try {
            Long userId = getUserId(userDetails);
            Long configId = configService.addConfig(userId, dto);
            logger.info("添加模型配置成功，配置ID: {}", configId);
            return Result.success("添加成功", configId);
        } catch (IllegalArgumentException e) {
            logger.warn("添加模型配置参数错误: {}", e.getMessage());
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            logger.error("添加模型配置异常", e);
            return Result.fail("添加模型配置失败");
        }
    }

    /**
     * 更新模型配置
     * 请求地址: PUT /ai/config/update
     * 测试数据示例:
     * {
     *   "id": 123456789,
     *   "configName": "更新后的配置名称",
     *   "temperature": 0.8
     * }
     */
    @PutMapping("/update")
    public Result<Void> update(@AuthenticationPrincipal UserDetails userDetails,
                               @Valid @RequestBody AiModelConfigUpdateDTO dto) {
        logger.info("更新模型配置请求，配置ID: {}", dto.getId());

        try {
            Long userId = getUserId(userDetails);
            configService.updateConfig(userId, dto);
            logger.info("更新模型配置成功，配置ID: {}", dto.getId());
            return Result.success();
        } catch (IllegalArgumentException e) {
            logger.warn("更新模型配置参数错误: {}", e.getMessage());
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            logger.error("更新模型配置异常", e);
            return Result.fail("更新模型配置失败");
        }
    }

    /**
     * 删除模型配置
     * 请求地址: DELETE /ai/config/delete/{id}
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@AuthenticationPrincipal UserDetails userDetails,
                               @PathVariable Long id) {
        logger.info("删除模型配置请求，配置ID: {}", id);

        try {
            Long userId = getUserId(userDetails);
            configService.deleteConfig(userId, id);
            logger.info("删除模型配置成功，配置ID: {}", id);
            return Result.success();
        } catch (IllegalArgumentException e) {
            logger.warn("删除模型配置参数错误: {}", e.getMessage());
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            logger.error("删除模型配置异常", e);
            return Result.fail("删除模型配置失败");
        }
    }

    /**
     * 设置默认模型
     * 请求地址: PUT /ai/config/set-default/{id}
     */
    @PutMapping("/set-default/{id}")
    public Result<Void> setDefault(@AuthenticationPrincipal UserDetails userDetails,
                                   @PathVariable Long id) {
        logger.info("设置默认模型请求，配置ID: {}", id);

        try {
            Long userId = getUserId(userDetails);
            configService.setDefault(userId, id);
            logger.info("设置默认模型成功，配置ID: {}", id);
            return Result.success();
        } catch (IllegalArgumentException e) {
            logger.warn("设置默认模型参数错误: {}", e.getMessage());
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            logger.error("设置默认模型异常", e);
            return Result.fail("设置默认模型失败");
        }
    }

    /**
     * 测试模型连接
     * 请求地址: POST /ai/config/test/{id}
     */
    @PostMapping("/test/{id}")
    public Result<String> test(@PathVariable Long id) {
        logger.info("测试模型连接请求，配置ID: {}", id);

        try {
            String result = configService.testConnection(id);
            logger.info("测试模型连接完成，配置ID: {}, 结果: {}", id, result);
            return Result.success(result);
        } catch (IllegalArgumentException e) {
            logger.warn("测试模型连接参数错误: {}", e.getMessage());
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            logger.error("测试模型连接异常", e);
            return Result.fail("测试模型连接失败: " + e.getMessage());
        }
    }

    /**
     * 从UserDetails获取用户ID
     * @param userDetails 用户详情
     * @return 用户ID
     */
    private Long getUserId(UserDetails userDetails) {
        // 这里需要根据实际的用户信息获取用户ID
        // 假设UserDetails中存储的是用户名，需要查询用户ID
        // 实际项目中应该从LoginUserDetails中获取
        if (userDetails instanceof com.example.evolutionary_ai_model.security.LoginUserDetails) {
            return ((com.example.evolutionary_ai_model.security.LoginUserDetails) userDetails).getUserId();
        }
        throw new IllegalArgumentException("无法获取用户信息");
    }
}