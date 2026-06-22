package com.example.evolutionary_ai_model.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * 文档解析服务接口，负责解析PDF、Word、TXT等文档格式。
 */
public interface DocumentParserService {

    /**
     * 解析文档文件，提取文本内容
     * @param file 上传的文件
     * @return 提取的文本内容
     */
    String parseDocument(MultipartFile file);

    /**
     * 解析文档文件，提取文本内容
     * @param inputStream 文件输入流
     * @param fileType 文件类型（pdf/docx/txt）
     * @return 提取的文本内容
     */
    String parseDocument(InputStream inputStream, String fileType);

    /**
     * 根据文件名获取文件类型
     * @param fileName 文件名
     * @return 文件类型（pdf/docx/txt）
     */
    String getFileType(String fileName);

    /**
     * 检查文件类型是否支持
     * @param fileType 文件类型
     * @return 是否支持
     */
    boolean isSupported(String fileType);
}
