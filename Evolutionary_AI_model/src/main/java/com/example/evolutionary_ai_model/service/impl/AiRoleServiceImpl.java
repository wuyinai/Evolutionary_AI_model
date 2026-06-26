package com.example.evolutionary_ai_model.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.evolutionary_ai_model.common.exception.BusinessException;
import com.example.evolutionary_ai_model.entity.AiRole;
import com.example.evolutionary_ai_model.entity.AiRoleDocument;
import com.example.evolutionary_ai_model.mapper.AiRoleDocumentMapper;
import com.example.evolutionary_ai_model.mapper.AiRoleMapper;
import com.example.evolutionary_ai_model.service.AiRoleService;
import com.example.evolutionary_ai_model.service.DocumentParserService;
import com.example.evolutionary_ai_model.service.MinioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用法：AI角色服务实现类，负责AI角色的创建、更新、删除、查询等核心业务逻辑。
 * 依赖 AiRoleMapper 进行数据持久化，依赖 MinioService 处理文件上传，依赖 DocumentParserService 解析文档。
 * 提供系统提示词构建功能，支持模板变量替换。
 */
@Service
public class AiRoleServiceImpl extends ServiceImpl<AiRoleMapper, AiRole>
        implements AiRoleService {

    private static final Logger logger = LoggerFactory.getLogger(AiRoleServiceImpl.class);

    @Autowired
    private AiRoleDocumentMapper documentMapper;

    @Autowired
    private MinioService minioService;

    @Autowired
    private DocumentParserService documentParserService;

    @Override
    public Long createRole(AiRole aiRole) {
        logger.info("创建AI角色，用户ID: {}, 名称: {}", aiRole.getUserId(), aiRole.getRoleName());

        // 设置默认值
        if (aiRole.getIsPublic() == null) {
            aiRole.setIsPublic(0); // 默认私有
        }
        if (aiRole.getStatus() == null) {
            aiRole.setStatus(1); // 默认启用
        }

        save(aiRole);
        logger.info("AI角色创建成功，ID: {}", aiRole.getId());
        return aiRole.getId();
    }

    @Override
    public void updateRole(AiRole aiRole) {
        logger.info("更新AI角色，ID: {}", aiRole.getId());

        // 检查角色是否存在
        AiRole existingRole = getById(aiRole.getId());
        if (existingRole == null) {
            throw new BusinessException("角色不存在，ID: " + aiRole.getId());
        }

        updateById(aiRole);
        logger.info("AI角色更新成功，ID: {}", aiRole.getId());
    }

    @Override
    @Transactional
    public void deleteRole(Long roleId) {
        logger.info("删除AI角色，ID: {}", roleId);

        // 检查角色是否存在
        AiRole aiRole = getById(roleId);
        if (aiRole == null) {
            throw new BusinessException("角色不存在，ID: " + roleId);
        }

        // 1. 获取角色下所有文档
        List<AiRoleDocument> documents = listDocuments(roleId);

        // 2. 删除MinIO中的文件
        for (AiRoleDocument document : documents) {
            if (document.getDocumentPath() != null && !document.getDocumentPath().isEmpty()) {
                try {
                    minioService.deleteFile(document.getDocumentPath());
                    logger.info("删除MinIO文件成功: {}", document.getDocumentPath());
                } catch (Exception e) {
                    logger.error("删除MinIO文件失败: {}", document.getDocumentPath(), e);
                    // 继续删除其他文件，不中断流程
                }
            }
        }

        // 3. 删除数据库中的文档记录
        documentMapper.deleteByRoleId(roleId);
        logger.info("删除角色文档记录，数量: {}", documents.size());

        // 4. 删除角色记录
        removeById(roleId);

        logger.info("AI角色删除成功，ID: {}，删除文档数: {}", roleId, documents.size());
    }

    @Override
    public AiRole getRoleById(Long roleId) {
        logger.info("获取AI角色详情，ID: {}", roleId);

        AiRole aiRole = getById(roleId);
        if (aiRole == null) {
            throw new BusinessException("角色不存在，ID: " + roleId);
        }

        // 加载关联文档列表
        List<AiRoleDocument> documents = listDocuments(roleId);
        aiRole.setDocuments(documents);

        return aiRole;
    }

    @Override
    public List<AiRole> getUserRoles(Long userId) {
        logger.info("获取用户角色列表，用户ID: {}", userId);

        return lambdaQuery()
                .eq(AiRole::getUserId, userId)
                .orderByDesc(AiRole::getCreateTime)
                .list();
    }

    @Override
    public Long uploadDocument(Long roleId, MultipartFile file) {
        logger.info("上传文档到角色，角色ID: {}, 文件名: {}", roleId, file.getOriginalFilename());

        // 检查角色是否存在
        AiRole aiRole = getById(roleId);
        if (aiRole == null) {
            throw new BusinessException("角色不存在，ID: " + roleId);
        }

        // 检查文件是否为空
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        // 获取文件类型
        String fileType = documentParserService.getFileType(file.getOriginalFilename());
        if (!documentParserService.isSupported(fileType)) {
            throw new BusinessException("不支持的文件类型: " + fileType);
        }

        try {
            // 1. 上传文件到MinIO
            String objectName = buildDocumentPath(roleId, file.getOriginalFilename());
            String documentPath = minioService.uploadFile(file, objectName);
            logger.info("文件上传到MinIO成功，路径: {}", documentPath);

            // 2. 解析文档内容
            String documentContent = documentParserService.parseDocument(file);
            logger.info("文档内容解析成功，长度: {}", documentContent.length());

            // 3. 创建文档记录
            AiRoleDocument document = new AiRoleDocument();
            document.setRoleId(roleId);
            document.setDocumentName(file.getOriginalFilename());
            document.setDocumentPath(objectName);
            document.setDocumentType(fileType);
            document.setDocumentSize(file.getSize());
            document.setDocumentContent(documentContent);
            document.setUploadTime(LocalDateTime.now());

            documentMapper.insert(document);
            logger.info("文档记录创建成功，ID: {}", document.getId());

            return document.getId();

        } catch (Exception e) {
            logger.error("上传文档失败，角色ID: {}", roleId, e);
            throw new BusinessException("上传文档失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteDocument(Long documentId) {
        logger.info("删除角色文档，文档ID: {}", documentId);

        // 获取文档信息
        AiRoleDocument document = documentMapper.selectById(documentId);
        if (document == null) {
            throw new BusinessException("文档不存在，ID: " + documentId);
        }

        try {
            // 1. 删除MinIO文件
            if (document.getDocumentPath() != null && !document.getDocumentPath().isEmpty()) {
                minioService.deleteFile(document.getDocumentPath());
                logger.info("删除MinIO文件成功: {}", document.getDocumentPath());
            }

            // 2. 删除数据库记录
            documentMapper.deleteById(documentId);

            logger.info("文档删除成功，文档ID: {}", documentId);

        } catch (Exception e) {
            logger.error("删除文档失败，文档ID: {}", documentId, e);
            throw new BusinessException("删除文档失败: " + e.getMessage());
        }
    }

    @Override
    public String buildSystemPrompt(Long roleId) {
        logger.info("构建系统提示词，角色ID: {}", roleId);

        // 获取角色信息
        AiRole aiRole = getById(roleId);
        if (aiRole == null) {
            logger.warn("角色不存在，返回默认提示词，ID: {}", roleId);
            return "你是一个智能助手。";
        }

        // 获取角色关联文档
        List<AiRoleDocument> documents = listDocuments(roleId);

        // 如果有系统提示词模板，使用模板进行变量替换
        if (aiRole.getSystemPromptTemplate() != null && !aiRole.getSystemPromptTemplate().isEmpty()) {
            String prompt = replaceTemplateVariables(aiRole, documents);
            logger.info("使用模板构建系统提示词，长度: {}", prompt.length());
            return prompt;
        }

        // 如果没有模板，但有纯文本系统提示词，直接使用
        if (aiRole.getSystemPrompt() != null && !aiRole.getSystemPrompt().isEmpty()) {
            logger.info("使用纯文本系统提示词，长度: {}", aiRole.getSystemPrompt().length());
            return aiRole.getSystemPrompt();
        }

        // 如果既没有模板也没有纯文本提示词，构建默认提示词
        String defaultPrompt = buildDefaultPrompt(aiRole, documents);
        logger.info("构建默认系统提示词，长度: {}", defaultPrompt.length());
        return defaultPrompt;
    }

    @Override
    public List<AiRoleDocument> listDocuments(Long roleId) {
        return documentMapper.selectList(
                new LambdaQueryWrapper<AiRoleDocument>()
                        .eq(AiRoleDocument::getRoleId, roleId)
                        .orderByDesc(AiRoleDocument::getUploadTime)
        );
    }

    @Override
    public AiRoleDocument getDocumentById(Long documentId) {
        logger.info("获取文档详情，文档ID: {}", documentId);

        AiRoleDocument document = documentMapper.selectById(documentId);
        if (document == null) {
            throw new BusinessException("文档不存在，ID: " + documentId);
        }
        return document;
    }

    @Override
    public String getDocumentPreviewUrl(Long documentId, int expiry) {
        logger.info("获取文档预览URL，文档ID: {}, 过期时间: {}秒", documentId, expiry);

        AiRoleDocument document = getDocumentById(documentId);
        if (document.getDocumentPath() == null || document.getDocumentPath().isEmpty()) {
            throw new BusinessException("文档路径不存在");
        }

        return minioService.getPresignedUrl(document.getDocumentPath(), expiry);
    }

    /**
     * 构建文档在MinIO中的存储路径
     * 格式：ai-role-documents/{roleId}/{timestamp}_{fileName}
     */
    private String buildDocumentPath(Long roleId, String fileName) {
        long timestamp = System.currentTimeMillis();
        return String.format("ai-role-documents/%d/%d_%s", roleId, timestamp, fileName);
    }

    /**
     * 替换模板变量
     * 支持的变量：{role_name}、{description}、{system_prompt}、{documents}、{document_1}、{document_2}等
     */
    private String replaceTemplateVariables(AiRole aiRole, List<AiRoleDocument> documents) {
        String template = aiRole.getSystemPromptTemplate();

        // 替换角色名称
        template = template.replace("{role_name}", aiRole.getRoleName() != null ? aiRole.getRoleName() : "");

        // 替换角色描述
        template = template.replace("{description}", aiRole.getDescription() != null ? aiRole.getDescription() : "");

        // 替换系统提示词
        template = template.replace("{system_prompt}", aiRole.getSystemPrompt() != null ? aiRole.getSystemPrompt() : "");

        // 替换所有文档内容（合并为一个字符串）
        if (documents != null && !documents.isEmpty()) {
            String allDocumentsContent = documents.stream()
                    .map(doc -> {
                        if (doc.getDocumentContent() != null) {
                            return String.format("【%s】\n%s", doc.getDocumentName(), doc.getDocumentContent());
                        }
                        return "";
                    })
                    .collect(Collectors.joining("\n\n"));
            template = template.replace("{documents}", allDocumentsContent);

            // 替换单个文档变量 {document_1}、{document_2} 等
            for (int i = 0; i < documents.size(); i++) {
                AiRoleDocument doc = documents.get(i);
                String documentContent = doc.getDocumentContent() != null ? doc.getDocumentContent() : "";
                template = template.replace("{document_" + (i + 1) + "}", documentContent);
            }
        } else {
            template = template.replace("{documents}", "");
        }

        return template;
    }

    /**
     * 构建默认系统提示词
     * 格式：角色名称 + 角色描述 + 文档内容
     */
    private String buildDefaultPrompt(AiRole aiRole, List<AiRoleDocument> documents) {
        StringBuilder promptBuilder = new StringBuilder();

        // 添加角色名称
        if (aiRole.getRoleName() != null && !aiRole.getRoleName().isEmpty()) {
            promptBuilder.append("你是一个名为").append(aiRole.getRoleName()).append("的AI角色。\n\n");
        } else {
            promptBuilder.append("你是一个智能助手。\n\n");
        }

        // 添加角色描述
        if (aiRole.getDescription() != null && !aiRole.getDescription().isEmpty()) {
            promptBuilder.append("角色描述：").append(aiRole.getDescription()).append("\n\n");
        }

        // 添加文档内容
        if (documents != null && !documents.isEmpty()) {
            promptBuilder.append("参考文档资料：\n");
            for (AiRoleDocument doc : documents) {
                if (doc.getDocumentContent() != null && !doc.getDocumentContent().isEmpty()) {
                    promptBuilder.append(String.format("【%s】\n%s\n\n", doc.getDocumentName(), doc.getDocumentContent()));
                }
            }
        }

        return promptBuilder.toString();
    }
}