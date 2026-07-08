package com.example.evolutionary_ai_model.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.evolutionary_ai_model.entity.SysPrompt;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用法：系统默认提示词服务接口，负责系统默认提示词的创建、更新、删除、查询等操作。
 * 提供文档上传、删除、文件预览等核心功能，用于约束智能体规范。
 */
public interface SysPromptService extends IService<SysPrompt> {

    /**
     * 创建提示词
     * @param sysPrompt 提示词信息
     * @return 提示词ID
     */
    Long createPrompt(SysPrompt sysPrompt);

    /**
     * 更新提示词
     * @param sysPrompt 提示词信息
     */
    void updatePrompt(SysPrompt sysPrompt);

    /**
     * 删除提示词（包括MinIO文件）
     * @param promptId 提示词ID
     */
    void deletePrompt(Long promptId);

    /**
     * 根据ID获取提示词详情
     * @param promptId 提示词ID
     * @return 提示词信息
     */
    SysPrompt getPromptById(Long promptId);

    /**
     * 获取所有提示词列表
     * @return 提示词列表
     */
    List<SysPrompt> listAllPrompts();

    /**
     * 上传文档型提示词
     * @param sysPrompt 提示词基本信息
     * @param file 文件
     * @return 提示词ID
     */
    Long uploadDocumentPrompt(SysPrompt sysPrompt, MultipartFile file);

    /**
     * 获取文档预览URL
     * @param promptId 提示词ID
     * @param expiry 过期时间（秒）
     * @return 预览URL
     */
    String getDocumentPreviewUrl(Long promptId, int expiry);

    /**
     * 获取默认提示词内容
     * @return 默认提示词内容
     */
    String getDefaultPromptContent();

    /**
     * 根据提示词代码获取提示词内容
     * @param promptCode 提示词代码
     * @return 提示词内容
     */
    String getPromptContentByCode(String promptCode);

    /**
     * 更新提示词启用状态
     * @param promptId 提示词ID
     * @param enabled 启用状态：0-禁用，1-启用
     */
    void updatePromptEnabled(Long promptId, Integer enabled);

    /**
     * 设置默认提示词
     * @param promptId 提示词ID
     */
    void setDefaultPrompt(Long promptId);
}