package com.example.evolutionary_ai_model.service;

import java.util.List;

/**
 * 文本分块服务接口，负责将长文本分割成适合向量化的片段。
 */
public interface TextChunkingService {

    /**
     * 将文本分块
     * @param text 原始文本
     * @param chunkSize 每个分块的最大字符数
     * @param overlap 分块之间的重叠字符数
     * @return 分块后的文本列表
     */
    List<String> chunkText(String text, int chunkSize, int overlap);

    /**
     * 使用默认参数将文本分块
     * @param text 原始文本
     * @return 分块后的文本列表
     */
    List<String> chunkText(String text);
}
