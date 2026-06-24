package com.example.evolutionary_ai_model.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.evolutionary_ai_model.common.exception.BusinessException;
import com.example.evolutionary_ai_model.entity.UserSkill;
import com.example.evolutionary_ai_model.entity.vo.UserSkillVO;
import com.example.evolutionary_ai_model.mapper.UserSkillMapper;
import com.example.evolutionary_ai_model.service.MinioService;
import com.example.evolutionary_ai_model.service.UserSkillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 用法：用户技能服务实现类，负责技能包的上传、解压、校验、查询等核心业务逻辑。
 * 依赖 UserSkillMapper 进行数据持久化，处理ZIP文件解压和SKILL.md解析。
 */
@Service
public class UserSkillServiceImpl extends ServiceImpl<UserSkillMapper, UserSkill>
        implements UserSkillService {

    private static final Logger logger = LoggerFactory.getLogger(UserSkillServiceImpl.class);

    @Autowired
    private MinioService minioService;

    @Override
    @Transactional
    public Long uploadSkill(Long userId, MultipartFile file) {
        logger.info("上传技能包，用户ID: {}, 文件名: {}", userId, file.getOriginalFilename());

        // 1. 校验文件类型
        if (!file.getOriginalFilename().endsWith(".zip")) {
            throw new BusinessException("只支持ZIP格式的技能包");
        }

        // 2. 解压ZIP文件到临时目录
        Path tempDir = null;
        String skillName = null;
        try {
            tempDir = Files.createTempDirectory("skill_upload_");
            unzipFile(file.getInputStream(), tempDir);

            // 3. 查找技能包根目录（包含SKILL.md的目录）
            Path skillRootDir = findSkillRootDir(tempDir);
            if (skillRootDir == null) {
                throw new BusinessException("技能包根目录必须包含SKILL.md文件");
            }
            logger.info("找到技能包根目录: {}", skillRootDir);

            // 4. 解析SKILL.md的YAML头信息
            Path skillMdPath = skillRootDir.resolve("SKILL.md");
            Map<String, String> skillMetadata = parseSkillMd(skillMdPath);
            skillName = skillMetadata.get("name");

            if (StrUtil.isBlank(skillName)) {
                throw new BusinessException("SKILL.md必须包含name字段");
            }

            // 5. 检查技能名称是否已存在
            if (existsByName(userId, skillName)) {
                throw new BusinessException("技能名称已存在: " + skillName);
            }

            // 6. 创建UserSkill对象并生成ID（使用雪花算法）
            UserSkill userSkill = new UserSkill();
            // 使用Hutool的雪花算法生成ID
            Long skillId = IdUtil.getSnowflakeNextId();
            userSkill.setId(skillId);
            userSkill.setUserId(userId);
            userSkill.setName(skillName);
            userSkill.setDisplayName(skillMetadata.get("displayName"));
            userSkill.setDescription(skillMetadata.getOrDefault("description", ""));
            userSkill.setVersion(skillMetadata.get("version"));
            userSkill.setAuthor(skillMetadata.get("author"));
            userSkill.setCreateBy(userId.toString());
            userSkill.setUpdateBy(userId.toString());
            userSkill.setEnabled(true);
            // 将Map转换为JSON字符串
            userSkill.setMetadata(JSONUtil.toJsonStr(skillMetadata));

            // 7. 上传文件到MinIO（路径: skills/{user_id}/{skill_id}/，保持原有目录结构）
            String basePath = "skills/" + userId + "/" + skillId;
            userSkill.setPath(basePath); // MinIO路径
            uploadSkillFilesToMinio(skillRootDir, basePath);

            // 8. 保存到数据库
            save(userSkill);
            logger.info("技能包上传成功，ID: {}, 名称: {}", userSkill.getId(), skillName);

            return userSkill.getId();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("上传技能包失败", e);
            throw new BusinessException("上传技能包失败: " + e.getMessage());
        } finally {
            // 清理临时目录
            if (tempDir != null) {
                try {
                    FileUtil.del(tempDir);
                } catch (Exception e) {
                    logger.warn("清理临时目录失败: {}", tempDir, e);
                }
            }
        }
    }

    @Override
    public List<UserSkillVO> listByUserId(Long userId) {
        logger.info("获取用户技能列表，用户ID: {}", userId);

        List<UserSkill> skills = lambdaQuery()
                .eq(UserSkill::getUserId, userId)
                .orderByDesc(UserSkill::getCreateTime)
                .list();

        return skills.stream().map(skill -> {
            UserSkillVO vo = new UserSkillVO();
            BeanUtils.copyProperties(skill, vo);
            return vo;
        }).toList();
    }

    @Override
    public UserSkillVO getSkillDetail(Long skillId) {
        logger.info("获取技能详情，ID: {}", skillId);

        UserSkill skill = getById(skillId);
        if (skill == null) {
            return null;
        }

        UserSkillVO vo = new UserSkillVO();
        BeanUtils.copyProperties(skill, vo);
        return vo;
    }

    @Override
    public void updateSkillStatus(Long skillId, Boolean enabled) {
        logger.info("更新技能状态，ID: {}, 启用: {}", skillId, enabled);

        UserSkill skill = getById(skillId);
        if (skill == null) {
            throw new BusinessException("技能不存在");
        }

        skill.setEnabled(enabled);
        updateById(skill);
        logger.info("技能状态更新成功");
    }

    @Override
    @Transactional
    public void deleteSkill(Long skillId) {
        logger.info("删除技能，ID: {}", skillId);

        UserSkill skill = getById(skillId);
        if (skill == null) {
            throw new BusinessException("技能不存在");
        }

        // 1. 删除MinIO中的文件
        deleteSkillFilesFromMinio(skill.getPath());

        // 2. 删除数据库记录
        removeById(skillId);
        logger.info("技能删除成功");
    }

    @Override
    public boolean existsByName(Long userId, String name) {
        return lambdaQuery()
                .eq(UserSkill::getUserId, userId)
                .eq(UserSkill::getName, name)
                .exists();
    }

    /**
     * 解压ZIP文件
     */
    private void unzipFile(InputStream inputStream, Path targetDir) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream, StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                Path entryPath = targetDir.resolve(entry.getName());

                // 防止ZIP slip攻击
                if (!entryPath.normalize().startsWith(targetDir.normalize())) {
                    throw new BusinessException("ZIP文件包含非法路径");
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    Files.copy(zipInputStream, entryPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zipInputStream.closeEntry();
            }
        }
        logger.info("ZIP文件解压完成，目录: {}", targetDir);
    }

    /**
     * 查找技能包根目录（包含SKILL.md的目录）
     * 支持两种情况：
     * 1. ZIP根目录直接包含SKILL.md
     * 2. ZIP包含一层子目录，子目录中有SKILL.md
     */
    private Path findSkillRootDir(Path dir) throws IOException {
        // 检查根目录是否有SKILL.md
        Path skillMdPath = dir.resolve("SKILL.md");
        if (Files.exists(skillMdPath)) {
            return dir;
        }

        // 如果根目录没有，检查是否有子目录（ZIP可能包含一层目录）
        try (var stream = Files.list(dir)) {
            for (Path subDir : stream.toList()) {
                if (Files.isDirectory(subDir)) {
                    Path subSkillMd = subDir.resolve("SKILL.md");
                    if (Files.exists(subSkillMd)) {
                        return subDir;
                    }
                }
            }
        }

        return null;
    }

    /**
     * 解析SKILL.md文件的YAML头信息
     */
    private Map<String, String> parseSkillMd(Path skillMdPath) throws IOException {
        String content = FileUtil.readUtf8String(skillMdPath.toFile());
        Map<String, String> metadata = new HashMap<>();

        // 解析YAML头（---之间的内容）
        if (content.startsWith("---")) {
            int endIndex = content.indexOf("---", 3);
            if (endIndex > 0) {
                String yamlContent = content.substring(3, endIndex).trim();
                String[] lines = yamlContent.split("\n");

                for (String line : lines) {
                    if (StrUtil.isBlank(line)) {
                        continue;
                    }

                    int colonIndex = line.indexOf(':');
                    if (colonIndex > 0) {
                        String key = line.substring(0, colonIndex).trim();
                        String value = line.substring(colonIndex + 1).trim();
                        metadata.put(key, value);
                    }
                }
            }
        }

        logger.info("解析SKILL.md完成，元数据: {}", metadata);
        return metadata;
    }

    /**
     * 上传技能文件到MinIO（保持原有目录结构）
     */
    private void uploadSkillFilesToMinio(Path skillRootDir, String basePath) throws IOException {
        logger.info("上传技能文件到MinIO，基础路径: {}, 根目录: {}", basePath, skillRootDir);

        // 遍历技能包根目录中的所有文件，保持原有目录结构
        Files.walk(skillRootDir)
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    try {
                        // 计算相对于技能包根目录的路径（保持目录结构）
                        String relativePath = skillRootDir.relativize(file).toString();
                        // Windows路径使用反斜杠，转换为正斜杠（MinIO/Unix路径）
                        relativePath = relativePath.replace('\\', '/');
                        // 对路径中的每个部分进行特殊字符处理（保留目录分隔符）
                        relativePath = sanitizePath(relativePath);
                        // 构建MinIO对象名称
                        String objectName = basePath + "/" + relativePath;

                        // 上传文件到MinIO
                        InputStream inputStream = Files.newInputStream(file);
                        long fileSize = Files.size(file);
                        // 根据文件扩展名获取contentType（更可靠的方式）
                        String contentType = getContentTypeByExtension(file.getFileName().toString());

                        minioService.uploadFile(inputStream, objectName, contentType, fileSize);
                        logger.info("上传文件成功: {}, contentType: {}", objectName, contentType);

                    } catch (Exception e) {
                        logger.error("上传文件失败: {}", file, e);
                        throw new BusinessException("上传文件到MinIO失败: " + e.getMessage());
                    }
                });

        logger.info("技能文件上传完成");
    }

    /**
     * 清理路径中的特殊字符（保留目录分隔符/）
     * 对每个路径部分单独处理，保持目录结构
     */
    private String sanitizePath(String path) {
        // 分割路径，对每个部分单独处理
        String[] parts = path.split("/");
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < parts.length; i++) {
            // 对每个路径部分进行特殊字符替换（保留字母、数字、点、下划线、连字符）
            String sanitized = parts[i].replaceAll("[^a-zA-Z0-9._-]", "_");
            result.append(sanitized);
            if (i < parts.length - 1) {
                result.append("/");
            }
        }
        
        return result.toString();
    }

    /**
     * 根据文件扩展名获取ContentType
     * Files.probeContentType()在某些系统上不可靠，手动映射更稳定
     */
    private String getContentTypeByExtension(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        
        return switch (extension) {
            case "md" -> "text/markdown";
            case "txt" -> "text/plain";
            case "json" -> "application/json";
            case "yaml", "yml" -> "application/x-yaml";
            case "py" -> "text/x-python";
            case "js" -> "application/javascript";
            case "ts" -> "application/typescript";
            case "html" -> "text/html";
            case "css" -> "text/css";
            case "xml" -> "application/xml";
            case "pdf" -> "application/pdf";
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "svg" -> "image/svg+xml";
            case "zip" -> "application/zip";
            case "csv" -> "text/csv";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            default -> "application/octet-stream";
        };
    }

    /**
     * 从MinIO删除技能文件
     */
    private void deleteSkillFilesFromMinio(String basePath) {
        logger.info("从MinIO删除技能文件，基础路径: {}", basePath);

        try {
            // 获取MinIO中该路径下的所有文件
            List<String> allFiles = minioService.listFiles();
            List<String> skillFiles = allFiles.stream()
                    .filter(fileName -> fileName.startsWith(basePath + "/"))
                    .toList();

            // 删除每个文件
            for (String fileName : skillFiles) {
                minioService.deleteFile(fileName);
                logger.info("删除文件: {}", fileName);
            }

            logger.info("技能文件删除完成，删除数量: {}", skillFiles.size());

        } catch (Exception e) {
            logger.warn("从MinIO删除技能文件失败: {}", basePath, e);
        }
    }
}