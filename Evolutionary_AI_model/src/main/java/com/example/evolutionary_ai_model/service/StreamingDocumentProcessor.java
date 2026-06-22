package com.example.evolutionary_ai_model.service;

import java.io.InputStream;
import java.util.function.Consumer;

/**
 * 流式文档处理器接口，支持边解析边分块，避免内存溢出。
 * 采用回调模式，每解析出一个分块就立即处理。
 */
public interface StreamingDocumentProcessor {

    /**
     * 流式处理文档，边解析边分块
     * @param inputStream 文件输入流
     * @param fileType 文件类型（pdf/docx/txt）
     * @param chunkSize 每个分块的最大字符数
     * @param overlap 分块之间的重叠字符数
     * @param chunkConsumer 分块消费者，每产生一个分块就调用此回调
     * @return 总分块数
     */
    int processStreaming(InputStream inputStream, String fileType, int chunkSize, int overlap, Consumer<String> chunkConsumer);

    /**
     * 使用默认参数流式处理文档
     * @param inputStream 文件输入流
     * @param fileType 文件类型（pdf/docx/txt）
     * @param chunkConsumer 分块消费者
     * @return 总分块数
     */
    int processStreaming(InputStream inputStream, String fileType, Consumer<String> chunkConsumer);
}
