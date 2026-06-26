package com.example.evolutionary_ai_model.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.evolutionary_ai_model.entity.AiRole;
import com.example.evolutionary_ai_model.entity.AiRoleDocument;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用法：AI角色服务接口，负责AI角色的创建、更新、删除、查询等操作。
 * 提供角色文档上传、删除、系统提示词构建等核心功能。
 */
public interface AiRoleService extends IService<AiRole> {

    /**
     * 创建AI角色
     * @param aiRole 角色信息
     * @return 角色ID
     */
    Long createRole(AiRole aiRole);

    /**
     * 更新AI角色
     * @param aiRole 角色信息
     */
    void updateRole(AiRole aiRole);

    /**
     * 删除AI角色（包括关联文档）
     * @param roleId 角色ID
     */
    void deleteRole(Long roleId);

    /**
     * 根据ID获取角色详情（包含文档列表）
     * @param roleId 角色ID
     * @return 角色信息
     */
    AiRole getRoleById(Long roleId);

    /**
     * 获取用户的角色列表
     * @param userId 用户ID
     * @return 角色列表
     */
    List<AiRole> getUserRoles(Long userId);

    /**
     * 上传文档到角色
     * @param roleId 角色ID
     * @param file 文件
     * @return 文档ID
     */
    Long uploadDocument(Long roleId, MultipartFile file);

    /**
     * 删除角色文档
     * @param documentId 文档ID
     */
    void deleteDocument(Long documentId);

    /**
     * 根据角色ID构建系统提示词
     * 支持模板变量替换：{role_name}、{description}、{system_prompt}、{documents}、{document_1}等
     * @param roleId 角色ID
     * @return 系统提示词
     */
    String buildSystemPrompt(Long roleId);

    /**
     * 获取角色下的文档列表
     * @param roleId 角色ID
     * @return 文档列表
     */
    List<AiRoleDocument> listDocuments(Long roleId);

    /**
     * 获取文档详情
     * @param documentId 文档ID
     * @return 文档信息
     */
    AiRoleDocument getDocumentById(Long documentId);

    /**
     * 获取文档预览URL
     * @param documentId 文档ID
     * @param expiry 过期时间（秒）
     * @return 预览URL
     */
    String getDocumentPreviewUrl(Long documentId, int expiry);
}