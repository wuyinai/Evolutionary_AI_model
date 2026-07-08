package com.example.evolutionary_ai_model.controller;

import com.example.evolutionary_ai_model.annotation.OperationLog;
import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.entity.SysPrompt;
import com.example.evolutionary_ai_model.security.LoginUserDetails;
import com.example.evolutionary_ai_model.service.SysPromptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用法：系统提示词Controller，负责接收前端系统提示词管理请求。
 * 位于表现层（Controller），处理系统提示词的创建、查询、更新、删除等操作。
 * 调用 SysPromptService 处理业务逻辑，返回统一响应格式 Result。
 * 支持文档型和文本型两种提示词类型，用于约束智能体规范。
 */
@RestController
@RequestMapping("/system/prompt")
public class SysPromptController {

    private static final Logger logger = LoggerFactory.getLogger(SysPromptController.class);

    private final SysPromptService sysPromptService;

    public SysPromptController(SysPromptService sysPromptService) {
        this.sysPromptService = sysPromptService;
    }

    /**
     * 上传文档型提示词
     * 请求地址: POST /system/prompt/upload
     * 测试数据: promptName=法律提示词, promptCode=legal_prompt, promptDescription=法律咨询提示词, file=(上传pdf/docx/txt文件)
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('sys:prompt:upload')")
    @OperationLog("上传文档型提示词")
    public Result<Long> uploadDocumentPrompt(@AuthenticationPrincipal UserDetails userDetails,
                                             @RequestParam("promptName") String promptName,
                                             @RequestParam("promptCode") String promptCode,
                                             @RequestParam(value = "promptDescription", required = false) String promptDescription,
                                             @RequestParam("file") MultipartFile file) {
        logger.info("上传文档型提示词请求，提示词名称: {}, 提示词代码: {}, 文件名: {}", promptName, promptCode, file.getOriginalFilename());

        try {
            // 构建提示词对象
            SysPrompt sysPrompt = new SysPrompt();
            sysPrompt.setPromptName(promptName);
            sysPrompt.setPromptCode(promptCode);
            sysPrompt.setPromptDescription(promptDescription);
            sysPrompt.setPromptType("DOCUMENT");

            // 获取当前用户作为创建人
            Long userId = getUserId(userDetails);
            sysPrompt.setCreateBy(String.valueOf(userId));

            // 上传文档型提示词
            Long promptId = sysPromptService.uploadDocumentPrompt(sysPrompt, file);
            logger.info("文档型提示词上传成功，提示词ID: {}", promptId);
            return Result.success("文档型提示词上传成功", promptId);
        } catch (Exception e) {
            logger.error("上传文档型提示词失败，提示词名称: {}, 提示词代码: {}", promptName, promptCode, e);
            return Result.fail("上传文档型提示词失败: " + e.getMessage());
        }
    }

    /**
     * 查询提示词列表
     * 请求地址: GET /system/prompt/list
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('sys:prompt:list')")
    public Result<List<SysPrompt>> listPrompts() {
        logger.info("查询系统提示词列表请求");

        try {
            List<SysPrompt> prompts = sysPromptService.listAllPrompts();
            logger.info("查询提示词列表成功，数量: {}", prompts.size());
            return Result.success(prompts);
        } catch (Exception e) {
            logger.error("查询提示词列表失败", e);
            return Result.fail("查询提示词列表失败: " + e.getMessage());
        }
    }

    /**
     * 查询提示词详情
     * 请求地址: GET /system/prompt/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:prompt:query')")
    public Result<SysPrompt> getPromptById(@PathVariable Long id) {
        logger.info("查询提示词详情请求，提示词ID: {}", id);

        try {
            SysPrompt sysPrompt = sysPromptService.getPromptById(id);
            logger.info("查询提示词详情成功，提示词ID: {}", id);
            return Result.success(sysPrompt);
        } catch (Exception e) {
            logger.error("查询提示词详情失败，提示词ID: {}", id, e);
            return Result.fail("查询提示词详情失败: " + e.getMessage());
        }
    }

    /**
     * 删除提示词
     * 请求地址: DELETE /system/prompt/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:prompt:delete')")
    @OperationLog("删除提示词")
    public Result<Void> deletePrompt(@PathVariable Long id) {
        logger.info("删除提示词请求，提示词ID: {}", id);

        try {
            sysPromptService.deletePrompt(id);
            logger.info("提示词删除成功，提示词ID: {}", id);
            return Result.success();
        } catch (Exception e) {
            logger.error("删除提示词失败，提示词ID: {}", id, e);
            return Result.fail("删除提示词失败: " + e.getMessage());
        }
    }

    /**
     * 获取文档预览URL
     * 请求地址: GET /system/prompt/preview/{id}
     * 测试数据: id=1, expiry=3600(可选，默认3600秒)
     */
    @GetMapping("/preview/{id}")
    @PreAuthorize("hasAuthority('sys:prompt:preview')")
    public Result<String> getDocumentPreviewUrl(@PathVariable Long id,
                                                @RequestParam(defaultValue = "3600") int expiry) {
        logger.info("获取文档预览URL请求，提示词ID: {}, 过期时间: {}秒", id, expiry);

        try {
            String previewUrl = sysPromptService.getDocumentPreviewUrl(id, expiry);
            logger.info("获取预览URL成功，提示词ID: {}", id);
            return Result.success(previewUrl);
        } catch (Exception e) {
            logger.error("获取预览URL失败，提示词ID: {}", id, e);
            return Result.fail("获取预览URL失败: " + e.getMessage());
        }
    }

    /**
     * 创建文本型提示词
     * 请求地址: POST /system/prompt/text
     * 测试数据: {"promptName": "通用助手提示词", "promptCode": "general_assistant", "promptDescription": "通用AI助手提示词", "textContent": "你是一个专业的AI助手..."}
     */
    @PostMapping("/text")
    @PreAuthorize("hasAuthority('sys:prompt:add')")
    @OperationLog("创建文本型提示词")
    public Result<Long> createTextPrompt(@AuthenticationPrincipal UserDetails userDetails,
                                         @RequestBody SysPrompt sysPrompt) {
        logger.info("创建文本型提示词请求，提示词名称: {}, 提示词代码: {}", sysPrompt.getPromptName(), sysPrompt.getPromptCode());

        try {
            // 设置提示词类型为文本型
            sysPrompt.setPromptType("TEXT");

            // 获取当前用户作为创建人
            Long userId = getUserId(userDetails);
            sysPrompt.setCreateBy(String.valueOf(userId));

            // 创建文本型提示词
            Long promptId = sysPromptService.createPrompt(sysPrompt);
            logger.info("文本型提示词创建成功，提示词ID: {}", promptId);
            return Result.success("文本型提示词创建成功", promptId);
        } catch (Exception e) {
            logger.error("创建文本型提示词失败，提示词名称: {}, 提示词代码: {}", sysPrompt.getPromptName(), sysPrompt.getPromptCode(), e);
            return Result.fail("创建文本型提示词失败: " + e.getMessage());
        }
    }

    /**
     * 更新提示词
     * 请求地址: PUT /system/prompt/{id}
     * 测试数据: {"promptName": "法律助手提示词-更新版", "promptDescription": "更新后的法律咨询提示词", "textContent": "你是一位专业的法律助手..."}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:prompt:edit')")
    @OperationLog("更新提示词")
    public Result<Void> updatePrompt(@AuthenticationPrincipal UserDetails userDetails,
                                     @PathVariable Long id,
                                     @RequestBody SysPrompt sysPrompt) {
        logger.info("更新提示词请求，提示词ID: {}", id);

        try {
            // 设置提示词ID
            sysPrompt.setId(id);

            // 获取当前用户作为更新人
            Long userId = getUserId(userDetails);
            sysPrompt.setUpdateBy(String.valueOf(userId));

            // 更新提示词
            sysPromptService.updatePrompt(sysPrompt);
            logger.info("提示词更新成功，提示词ID: {}", id);
            return Result.success();
        } catch (Exception e) {
            logger.error("更新提示词失败，提示词ID: {}", id, e);
            return Result.fail("更新提示词失败: " + e.getMessage());
        }
    }

    /**
     * 更新提示词启用状态
     * 请求地址: PUT /system/prompt/{id}/enabled?enabled=1
     * 测试数据: id=1, enabled=1(启用) 或 enabled=0(禁用)
     */
    @PutMapping("/{id}/enabled")
    @PreAuthorize("hasAuthority('sys:prompt:edit')")
    @OperationLog("更新提示词启用状态")
    public Result<Void> updatePromptEnabled(@PathVariable Long id,
                                            @RequestParam("enabled") Integer enabled) {
        logger.info("更新提示词启用状态请求，提示词ID: {}, 启用状态: {}", id, enabled);

        try {
            sysPromptService.updatePromptEnabled(id, enabled);
            logger.info("提示词启用状态更新成功，提示词ID: {}", id);
            return Result.success();
        } catch (Exception e) {
            logger.error("更新提示词启用状态失败，提示词ID: {}", id, e);
            return Result.fail("更新提示词启用状态失败: " + e.getMessage());
        }
    }

    /**
     * 设置默认提示词
     * 请求地址: PUT /system/prompt/{id}/default
     * 测试数据: id=1
     */
    @PutMapping("/{id}/default")
    @PreAuthorize("hasAuthority('sys:prompt:edit')")
    @OperationLog("设置默认提示词")
    public Result<Void> setDefaultPrompt(@PathVariable Long id) {
        logger.info("设置默认提示词请求，提示词ID: {}", id);

        try {
            sysPromptService.setDefaultPrompt(id);
            logger.info("默认提示词设置成功，提示词ID: {}", id);
            return Result.success();
        } catch (Exception e) {
            logger.error("设置默认提示词失败，提示词ID: {}", id, e);
            return Result.fail("设置默认提示词失败: " + e.getMessage());
        }
    }

    /**
     * 从UserDetails中获取用户ID
     */
    private Long getUserId(UserDetails userDetails) {
        if (userDetails instanceof LoginUserDetails) {
            return ((LoginUserDetails) userDetails).getUserId();
        }
        throw new IllegalArgumentException("无法获取用户信息");
    }
}