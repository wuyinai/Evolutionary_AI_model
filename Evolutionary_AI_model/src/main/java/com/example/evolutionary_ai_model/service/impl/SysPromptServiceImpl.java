package com.example.evolutionary_ai_model.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.evolutionary_ai_model.common.exception.BusinessException;
import com.example.evolutionary_ai_model.entity.SysPrompt;
import com.example.evolutionary_ai_model.mapper.SysPromptMapper;
import com.example.evolutionary_ai_model.service.DocumentParserService;
import com.example.evolutionary_ai_model.service.MinioService;
import com.example.evolutionary_ai_model.service.SysPromptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用法：系统默认提示词服务实现类，负责系统默认提示词的创建、更新、删除、查询等核心业务逻辑。
 * 依赖 SysPromptMapper 进行数据持久化，依赖 MinioService 处理文档上传与删除，
 * 依赖 DocumentParserService 解析文档内容为文本。
 * 支持文档型（DOCUMENT）和文本型（TEXT）两种提示词类型，用于约束智能体规范。
 *
 * 职责：
 * 1. 管理系统默认提示词的CRUD操作
 * 2. 处理文档型提示词的MinIO上传与删除
 * 3. 解析文档内容为可用的提示词文本
 * 4. 提供默认提示词和按标识码查询提示词的功能
 *
 * 调用链路：
 * Controller -> SysPromptService -> SysPromptMapper
 *           -> MinioService (文档操作)
 *           -> DocumentParserService (文档解析)
 */
@Service
public class SysPromptServiceImpl extends ServiceImpl<SysPromptMapper, SysPrompt>
        implements SysPromptService {

    private static final Logger logger = LoggerFactory.getLogger(SysPromptServiceImpl.class);

    @Autowired
    private MinioService minioService;

    @Autowired
    private DocumentParserService documentParserService;

    @Override
    public Long createPrompt(SysPrompt sysPrompt) {
        logger.info("创建系统提示词，名称: {}, 类型: {}, 编码: {}",
                sysPrompt.getPromptName(), sysPrompt.getPromptType(), sysPrompt.getPromptCode());

        // 校验提示词编码唯一性
        if (sysPrompt.getPromptCode() != null && !sysPrompt.getPromptCode().isEmpty()) {
            long count = lambdaQuery()
                    .eq(SysPrompt::getPromptCode, sysPrompt.getPromptCode())
                    .count();
            if (count > 0) {
                logger.warn("提示词编码已存在: {}", sysPrompt.getPromptCode());
                throw new BusinessException("提示词编码已存在: " + sysPrompt.getPromptCode());
            }
        }

        // 设置默认值
        if (sysPrompt.getIsEnabled() == null) {
            sysPrompt.setIsEnabled(1); // 默认启用
        }
        if (sysPrompt.getIsDefault() == null) {
            sysPrompt.setIsDefault(0); // 默认非默认提示词
        }
        if (sysPrompt.getSortOrder() == null) {
            sysPrompt.setSortOrder(0); // 默认排序号
        }
        if (sysPrompt.getCreateTime() == null) {
            sysPrompt.setCreateTime(LocalDateTime.now());
        }

        save(sysPrompt);
        logger.info("系统提示词创建成功，ID: {}", sysPrompt.getId());
        return sysPrompt.getId();
    }

    @Override
    public void updatePrompt(SysPrompt sysPrompt) {
        logger.info("更新系统提示词，ID: {}", sysPrompt.getId());

        // 检查提示词是否存在
        SysPrompt existingPrompt = getById(sysPrompt.getId());
        if (existingPrompt == null) {
            throw new BusinessException("提示词不存在，ID: " + sysPrompt.getId());
        }

        // 如果修改了编码，需校验唯一性
        if (sysPrompt.getPromptCode() != null && !sysPrompt.getPromptCode().isEmpty()) {
            if (!sysPrompt.getPromptCode().equals(existingPrompt.getPromptCode())) {
                long count = lambdaQuery()
                        .eq(SysPrompt::getPromptCode, sysPrompt.getPromptCode())
                        .ne(SysPrompt::getId, sysPrompt.getId())
                        .count();
                if (count > 0) {
                    logger.warn("提示词编码已存在: {}", sysPrompt.getPromptCode());
                    throw new BusinessException("提示词编码已存在: " + sysPrompt.getPromptCode());
                }
            }
        }

        // 更新时间
        sysPrompt.setUpdateTime(LocalDateTime.now());

        updateById(sysPrompt);
        logger.info("系统提示词更新成功，ID: {}", sysPrompt.getId());
    }

    @Override
    @Transactional
    public void deletePrompt(Long promptId) {
        logger.info("删除系统提示词，ID: {}", promptId);

        // 检查提示词是否存在
        SysPrompt sysPrompt = getById(promptId);
        if (sysPrompt == null) {
            throw new BusinessException("提示词不存在，ID: " + promptId);
        }

        // 如果是文档型提示词，需要删除MinIO中的文件
        if ("DOCUMENT".equals(sysPrompt.getPromptType())
                && sysPrompt.getDocumentPath() != null
                && !sysPrompt.getDocumentPath().isEmpty()) {
            try {
                minioService.deleteFile(sysPrompt.getDocumentPath());
                logger.info("删除MinIO文件成功: {}", sysPrompt.getDocumentPath());
            } catch (Exception e) {
                logger.error("删除MinIO文件失败: {}", sysPrompt.getDocumentPath(), e);
                // 继续删除数据库记录，不中断流程
            }
        }

        // 删除数据库记录
        removeById(promptId);

        logger.info("系统提示词删除成功，ID: {}", promptId);
    }

    @Override
    public SysPrompt getPromptById(Long promptId) {
        logger.info("获取系统提示词详情，ID: {}", promptId);

        SysPrompt sysPrompt = getById(promptId);
        if (sysPrompt == null) {
            throw new BusinessException("提示词不存在，ID: " + promptId);
        }

        return sysPrompt;
    }

    @Override
    public List<SysPrompt> listAllPrompts() {
        logger.info("获取所有系统提示词列表");

        List<SysPrompt> prompts = lambdaQuery()
                .orderByAsc(SysPrompt::getSortOrder)
                .orderByDesc(SysPrompt::getCreateTime)
                .list();

        logger.info("获取系统提示词列表成功，数量: {}", prompts.size());
        return prompts;
    }

    @Override
    @Transactional
    public Long uploadDocumentPrompt(SysPrompt sysPrompt, MultipartFile file) {
        logger.info("上传文档型提示词，名称: {}, 文件名: {}",
                sysPrompt.getPromptName(), file.getOriginalFilename());

        // 检查文件是否为空
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        // 检查文件类型是否支持
        String fileType = documentParserService.getFileType(file.getOriginalFilename());
        if (!documentParserService.isSupported(fileType)) {
            throw new BusinessException("不支持的文件类型: " + fileType);
        }

        // 设置提示词类型为文档型
        sysPrompt.setPromptType("DOCUMENT");

        // 校验提示词编码唯一性
        if (sysPrompt.getPromptCode() != null && !sysPrompt.getPromptCode().isEmpty()) {
            long count = lambdaQuery()
                    .eq(SysPrompt::getPromptCode, sysPrompt.getPromptCode())
                    .count();
            if (count > 0) {
                logger.warn("提示词编码已存在: {}", sysPrompt.getPromptCode());
                throw new BusinessException("提示词编码已存在: " + sysPrompt.getPromptCode());
            }
        }

        try {
            // 1. 上传文件到MinIO
            String objectName = buildDocumentPath(sysPrompt.getPromptCode(), file.getOriginalFilename());
            String documentPath = minioService.uploadFile(file, objectName);
            logger.info("文件上传到MinIO成功，路径: {}", documentPath);

            // 2. 解析文档内容
            String documentContent = documentParserService.parseDocument(file);
            logger.info("文档内容解析成功，长度: {}", documentContent.length());

            // 3. 设置文档信息
            sysPrompt.setDocumentName(file.getOriginalFilename());
            sysPrompt.setDocumentPath(objectName);
            sysPrompt.setDocumentType(fileType);
            sysPrompt.setDocumentSize(file.getSize());
            sysPrompt.setDocumentContent(documentContent);
            sysPrompt.setUploadTime(LocalDateTime.now());

            // 4. 设置默认值
            if (sysPrompt.getIsEnabled() == null) {
                sysPrompt.setIsEnabled(1);
            }
            if (sysPrompt.getIsDefault() == null) {
                sysPrompt.setIsDefault(0);
            }
            if (sysPrompt.getSortOrder() == null) {
                sysPrompt.setSortOrder(0);
            }
            if (sysPrompt.getCreateTime() == null) {
                sysPrompt.setCreateTime(LocalDateTime.now());
            }

            // 5. 保存到数据库
            save(sysPrompt);
            logger.info("文档型提示词创建成功，ID: {}", sysPrompt.getId());

            return sysPrompt.getId();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("上传文档型提示词失败，名称: {}", sysPrompt.getPromptName(), e);
            throw new BusinessException("上传文档型提示词失败: " + e.getMessage());
        }
    }

    @Override
    public String getDocumentPreviewUrl(Long promptId, int expiry) {
        logger.info("获取文档预览URL，提示词ID: {}, 过期时间: {}秒", promptId, expiry);

        // 获取提示词信息
        SysPrompt sysPrompt = getPromptById(promptId);

        // 检查是否为文档型提示词
        if (!"DOCUMENT".equals(sysPrompt.getPromptType())) {
            throw new BusinessException("非文档型提示词，无法获取预览URL");
        }

        // 检查文档路径是否存在
        if (sysPrompt.getDocumentPath() == null || sysPrompt.getDocumentPath().isEmpty()) {
            throw new BusinessException("文档路径不存在");
        }

        // 生成预览URL
        String previewUrl = minioService.getPresignedUrl(sysPrompt.getDocumentPath(), expiry);
        logger.info("生成文档预览URL成功，提示词ID: {}", promptId);

        return previewUrl;
    }

    @Override
    public String getDefaultPromptContent() {
        logger.info("获取默认提示词内容");

        // 查询is_default=1的提示词
        SysPrompt defaultPrompt = lambdaQuery()
                .eq(SysPrompt::getIsDefault, 1)
                .eq(SysPrompt::getIsEnabled, 1)
                .one();

        if (defaultPrompt == null) {
            logger.warn("未找到默认提示词");
            return null;
        }

        // 根据提示词类型返回内容
        String content = getPromptContent(defaultPrompt);
        logger.info("获取默认提示词内容成功，ID: {}, 类型: {}, 内容长度: {}",
                defaultPrompt.getId(), defaultPrompt.getPromptType(),
                content != null ? content.length() : 0);

        return content;
    }

    @Override
    public String getPromptContentByCode(String promptCode) {
        logger.info("根据编码获取提示词内容，编码: {}", promptCode);

        // 参数校验
        if (promptCode == null || promptCode.isEmpty()) {
            throw new BusinessException("提示词编码不能为空");
        }

        // 根据编码查询提示词
        SysPrompt sysPrompt = lambdaQuery()
                .eq(SysPrompt::getPromptCode, promptCode)
                .eq(SysPrompt::getIsEnabled, 1)
                .one();

        if (sysPrompt == null) {
            logger.warn("未找到提示词，编码: {}", promptCode);
            return null;
        }

        // 根据提示词类型返回内容
        String content = getPromptContent(sysPrompt);
        logger.info("获取提示词内容成功，ID: {}, 类型: {}, 内容长度: {}",
                sysPrompt.getId(), sysPrompt.getPromptType(),
                content != null ? content.length() : 0);

        return content;
    }

    /**
     * 构建文档在MinIO中的存储路径
     * 格式：sys-prompt-documents/{promptCode}/{timestamp}_{fileName}
     *
     * @param promptCode 提示词编码
     * @param fileName 文件名
     * @return MinIO对象名称（路径）
     */
    private String buildDocumentPath(String promptCode, String fileName) {
        long timestamp = System.currentTimeMillis();
        String code = (promptCode != null && !promptCode.isEmpty()) ? promptCode : "default";
        return String.format("sys-prompt-documents/%s/%d_%s", code, timestamp, fileName);
    }

    /**
     * 根据提示词类型获取提示词内容
     * 文档型返回documentContent，文本型返回textContent
     *
     * @param sysPrompt 提示词对象
     * @return 提示词内容
     */
    private String getPromptContent(SysPrompt sysPrompt) {
        if ("DOCUMENT".equals(sysPrompt.getPromptType())) {
            return sysPrompt.getDocumentContent();
        } else if ("TEXT".equals(sysPrompt.getPromptType())) {
            return sysPrompt.getTextContent();
        }
        logger.warn("未知的提示词类型: {}", sysPrompt.getPromptType());
        return null;
    }

    @Override
    @Transactional
    public void updatePromptEnabled(Long promptId, Integer enabled) {
        logger.info("更新提示词启用状态，ID: {}, 启用状态: {}", promptId, enabled);

        // 检查提示词是否存在
        SysPrompt sysPrompt = getById(promptId);
        if (sysPrompt == null) {
            throw new BusinessException("提示词不存在，ID: " + promptId);
        }

        // 更新启用状态
        sysPrompt.setIsEnabled(enabled);
        sysPrompt.setUpdateTime(LocalDateTime.now());
        updateById(sysPrompt);

        logger.info("提示词启用状态更新成功，ID: {}", promptId);
    }

    @Override
    @Transactional
    public void setDefaultPrompt(Long promptId) {
        logger.info("设置默认提示词，ID: {}", promptId);

        // 检查提示词是否存在
        SysPrompt sysPrompt = getById(promptId);
        if (sysPrompt == null) {
            throw new BusinessException("提示词不存在，ID: " + promptId);
        }

        // 检查提示词是否已启用
        if (sysPrompt.getIsEnabled() != 1) {
            throw new BusinessException("只能设置已启用的提示词为默认提示词");
        }

        // 先取消所有现有的默认提示词
        lambdaUpdate()
                .set(SysPrompt::getIsDefault, 0)
                .eq(SysPrompt::getIsDefault, 1)
                .update();

        // 设置新的默认提示词
        sysPrompt.setIsDefault(1);
        sysPrompt.setUpdateTime(LocalDateTime.now());
        updateById(sysPrompt);

        logger.info("默认提示词设置成功，ID: {}", promptId);
    }
}