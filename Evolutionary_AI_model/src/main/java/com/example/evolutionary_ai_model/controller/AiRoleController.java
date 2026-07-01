package com.example.evolutionary_ai_model.controller;

import com.example.evolutionary_ai_model.annotation.OperationLog;
import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.entity.AiRole;
import com.example.evolutionary_ai_model.entity.AiRoleDocument;
import com.example.evolutionary_ai_model.security.LoginUserDetails;
import com.example.evolutionary_ai_model.service.AiRoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用法：AI角色Controller，负责接收前端AI角色管理请求。
 * 提供角色的创建、查询、更新、删除等功能，以及文档上传、删除、预览等功能。
 */
@RestController
@RequestMapping("/ai-role")
public class AiRoleController {

    private static final Logger logger = LoggerFactory.getLogger(AiRoleController.class);

    private final AiRoleService aiRoleService;

    public AiRoleController(AiRoleService aiRoleService) {
        this.aiRoleService = aiRoleService;
    }

    /**
     * 创建AI角色
     * 请求地址: POST /ai-role
     * 测试数据: {"roleName": "律师助手", "roleCode": "lawyer", "description": "法律咨询AI角色", "systemPrompt": "你是一位专业的律师助手"}
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ai:role:add')")
    public Result<Long> createRole(@AuthenticationPrincipal UserDetails userDetails,
                                   @RequestBody AiRole aiRole) {
        logger.info("创建AI角色请求，名称: {}", aiRole.getRoleName());

        try {
            Long userId = getUserId(userDetails);
            aiRole.setUserId(userId);
            Long roleId = aiRoleService.createRole(aiRole);
            logger.info("AI角色创建成功，ID: {}", roleId);
            return Result.success("AI角色创建成功", roleId);
        } catch (Exception e) {
            logger.error("创建AI角色失败", e);
            return Result.fail("创建AI角色失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的角色列表
     * 请求地址: GET /ai-role/list
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('ai:role:list')")
    public Result<List<AiRole>> listRoles(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("获取AI角色列表请求");

        try {
            Long userId = getUserId(userDetails);
            List<AiRole> roles = aiRoleService.getUserRoles(userId);
            logger.info("获取角色列表成功，数量: {}", roles.size());
            return Result.success(roles);
        } catch (Exception e) {
            logger.error("获取角色列表失败", e);
            return Result.fail("获取角色列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取角色详情
     * 请求地址: GET /ai-role/{roleId}
     */
    @GetMapping("/{roleId}")
    @PreAuthorize("hasAuthority('ai:role:list')")
    public Result<AiRole> getRole(@PathVariable Long roleId) {
        logger.info("获取AI角色详情请求，ID: {}", roleId);

        try {
            AiRole aiRole = aiRoleService.getRoleById(roleId);
            return Result.success(aiRole);
        } catch (Exception e) {
            logger.error("获取角色详情失败", e);
            return Result.fail("获取角色详情失败: " + e.getMessage());
        }
    }

    /**
     * 更新角色信息
     * 请求地址: PUT /ai-role
     * 测试数据: {"id": 1, "roleName": "律师助手-更新版", "description": "更新后的描述"}
     */
    @PutMapping
    @PreAuthorize("hasAuthority('ai:role:edit')")
    @OperationLog("更新AI角色")
    public Result<Void> updateRole(@RequestBody AiRole aiRole) {
        logger.info("更新AI角色请求，ID: {}", aiRole.getId());

        try {
            aiRoleService.updateRole(aiRole);
            logger.info("AI角色更新成功，ID: {}", aiRole.getId());
            return Result.success();
        } catch (Exception e) {
            logger.error("更新角色失败", e);
            return Result.fail("更新角色失败: " + e.getMessage());
        }
    }

    /**
     * 删除角色
     * 请求地址: DELETE /ai-role/{roleId}
     */
    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAuthority('ai:role:delete')")
    public Result<Void> deleteRole(@PathVariable Long roleId) {
        logger.info("删除AI角色请求，ID: {}", roleId);

        try {
            aiRoleService.deleteRole(roleId);
            logger.info("AI角色删除成功，ID: {}", roleId);
            return Result.success();
        } catch (Exception e) {
            logger.error("删除角色失败", e);
            return Result.fail("删除角色失败: " + e.getMessage());
        }
    }

    /**
     * 更新角色状态（启用/禁用）
     * 请求地址: PUT /ai-role/{roleId}/status
     */
    @PutMapping("/{roleId}/status")
    @PreAuthorize("hasAuthority('ai:role:edit')")
    public Result<Void> updateRoleStatus(@PathVariable Long roleId,
                                         @RequestParam Integer status) {
        logger.info("更新角色状态请求，ID: {}, 新状态: {}", roleId, status);

        try {
            // 验证状态值
            if (status != 0 && status != 1) {
                return Result.fail("状态值必须为0(禁用)或1(启用)");
            }

            // 构建更新对象
            AiRole aiRole = new AiRole();
            aiRole.setId(roleId);
            aiRole.setStatus(status);

            aiRoleService.updateRole(aiRole);
            logger.info("角色状态更新成功，ID: {}, 状态: {}", roleId, status);
            return Result.success();
        } catch (Exception e) {
            logger.error("更新角色状态失败", e);
            return Result.fail("更新角色状态失败: " + e.getMessage());
        }
    }

    /**
     * 上传文档到角色
     * 请求地址: POST /ai-role/{roleId}/document
     */
    @PostMapping("/{roleId}/document")
    @PreAuthorize("hasAuthority('ai:role:document:add')")
    public Result<Long> uploadDocument(@PathVariable Long roleId,
                                       @RequestParam("file") MultipartFile file) {
        logger.info("上传文档到角色请求，角色ID: {}, 文件名: {}", roleId, file.getOriginalFilename());

        try {
            Long documentId = aiRoleService.uploadDocument(roleId, file);
            logger.info("文档上传成功，文档ID: {}", documentId);
            return Result.success("文档上传成功", documentId);
        } catch (Exception e) {
            logger.error("上传文档失败", e);
            return Result.fail("上传文档失败: " + e.getMessage());
        }
    }

    /**
     * 删除角色文档
     * 请求地址: DELETE /ai-role/document/{documentId}
     */
    @DeleteMapping("/document/{documentId}")
    @PreAuthorize("hasAuthority('ai:role:document:delete')")
    @OperationLog("删除角色文档")
    public Result<Void> deleteDocument(@PathVariable Long documentId) {
        logger.info("删除角色文档请求，文档ID: {}", documentId);

        try {
            aiRoleService.deleteDocument(documentId);
            logger.info("文档删除成功，文档ID: {}", documentId);
            return Result.success();
        } catch (Exception e) {
            logger.error("删除文档失败", e);
            return Result.fail("删除文档失败: " + e.getMessage());
        }
    }

    /**
     * 获取角色下的文档列表
     * 请求地址: GET /ai-role/{roleId}/documents
     */
    @GetMapping("/{roleId}/documents")
    @PreAuthorize("hasAuthority('ai:role:document:list')")
    public Result<List<AiRoleDocument>> listDocuments(@PathVariable Long roleId) {
        logger.info("获取角色文档列表请求，角色ID: {}", roleId);

        try {
            List<AiRoleDocument> documents = aiRoleService.listDocuments(roleId);
            logger.info("获取文档列表成功，数量: {}", documents.size());
            return Result.success(documents);
        } catch (Exception e) {
            logger.error("获取文档列表失败", e);
            return Result.fail("获取文档列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取文档详情
     * 请求地址: GET /ai-role/document/{documentId}
     */
    @GetMapping("/document/{documentId}")
    @PreAuthorize("hasAuthority('ai:role:document:list')")
    public Result<AiRoleDocument> getDocument(@PathVariable Long documentId) {
        logger.info("获取文档详情请求，文档ID: {}", documentId);

        try {
            AiRoleDocument document = aiRoleService.getDocumentById(documentId);
            return Result.success(document);
        } catch (Exception e) {
            logger.error("获取文档详情失败", e);
            return Result.fail("获取文档详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取文档预览URL
     * 请求地址: GET /ai-role/document/{documentId}/preview
     */
    @GetMapping("/document/{documentId}/preview")
    @PreAuthorize("hasAuthority('ai:role:document:list')")
    public Result<String> getDocumentPreviewUrl(@PathVariable Long documentId,
                                                @RequestParam(defaultValue = "3600") int expiry) {
        logger.info("获取文档预览URL请求，文档ID: {}, 过期时间: {}秒", documentId, expiry);

        try {
            String previewUrl = aiRoleService.getDocumentPreviewUrl(documentId, expiry);
            logger.info("获取预览URL成功");
            return Result.success(previewUrl);
        } catch (Exception e) {
            logger.error("获取预览URL失败", e);
            return Result.fail("获取预览URL失败: " + e.getMessage());
        }
    }

    /**
     * 预览角色系统提示词
     * 请求地址: GET /ai-role/{roleId}/preview-prompt
     */
    @GetMapping("/{roleId}/preview-prompt")
    @PreAuthorize("hasAuthority('ai:role:list')")
    public Result<String> previewSystemPrompt(@PathVariable Long roleId) {
        logger.info("预览系统提示词请求，角色ID: {}", roleId);

        try {
            String systemPrompt = aiRoleService.buildSystemPrompt(roleId);
            logger.info("系统提示词构建成功，长度: {}", systemPrompt.length());
            return Result.success(systemPrompt);
        } catch (Exception e) {
            logger.error("预览系统提示词失败", e);
            return Result.fail("预览系统提示词失败: " + e.getMessage());
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