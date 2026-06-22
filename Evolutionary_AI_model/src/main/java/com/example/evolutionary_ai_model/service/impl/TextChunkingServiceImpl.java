package com.example.evolutionary_ai_model.service.impl;

import com.example.evolutionary_ai_model.service.TextChunkingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 文本分块服务实现类，使用滑动窗口算法进行文本分块。
 */
@Service
public class TextChunkingServiceImpl implements TextChunkingService {

    private static final Logger logger = LoggerFactory.getLogger(TextChunkingServiceImpl.class);

    // 默认分块大小（字符数）
    private static final int DEFAULT_CHUNK_SIZE = 500;

    // 默认重叠大小（字符数）
    private static final int DEFAULT_OVERLAP = 50;

    @Override
    public List<String> chunkText(String text, int chunkSize, int overlap) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }

        if (chunkSize <= 0) {
            throw new IllegalArgumentException("分块大小必须大于0");
        }

        if (overlap < 0 || overlap >= chunkSize) {
            throw new IllegalArgumentException("重叠大小必须大于等于0且小于分块大小");
        }

        List<String> chunks = new ArrayList<>();
        int textLength = text.length();
        int start = 0;

        while (start < textLength) {
            int end = Math.min(start + chunkSize, textLength);

            // 如果不是最后一块，尝试在句子边界处分块
            if (end < textLength) {
                int lastPeriod = text.lastIndexOf('。', end);
                int lastExclamation = text.lastIndexOf('！', end);
                int lastQuestion = text.lastIndexOf('？', end);
                int lastNewLine = text.lastIndexOf('\n', end);

                int boundary = Math.max(Math.max(lastPeriod, lastExclamation),
                                       Math.max(lastQuestion, lastNewLine));

                if (boundary > start) {
                    end = boundary + 1;
                }
            }

            String chunk = text.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            // 移动到下一个分块的起始位置（考虑重叠）
            start = end - overlap;
            if (start < 0) {
                start = end; // 避免负数
            }
        }

        logger.info("文本分块完成，总长度: {}, 分块数: {}", textLength, chunks.size());
        return chunks;
    }

    @Override
    public List<String> chunkText(String text) {
        return chunkText(text, DEFAULT_CHUNK_SIZE, DEFAULT_OVERLAP);
    }
}
