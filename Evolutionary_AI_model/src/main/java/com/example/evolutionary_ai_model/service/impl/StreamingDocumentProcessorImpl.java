package com.example.evolutionary_ai_model.service.impl;

import com.example.evolutionary_ai_model.service.StreamingDocumentProcessor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * 流式文档处理器实现类，支持边解析边分块，避免内存溢出。
 * 核心思路：
 * 1. 对于PDF，按页解析，每页文本累积到缓冲区，达到分块大小时输出
 * 2. 对于TXT，按行读取，累积到缓冲区，达到分块大小时输出
 * 3. 对于Word，按段落读取，累积到缓冲区，达到分块大小时输出
 */
@Service
public class StreamingDocumentProcessorImpl implements StreamingDocumentProcessor {

    private static final Logger logger = LoggerFactory.getLogger(StreamingDocumentProcessorImpl.class);

    // 默认分块大小
    private static final int DEFAULT_CHUNK_SIZE = 500;
    // 默认重叠大小
    private static final int DEFAULT_OVERLAP = 50;

    @Override
    public int processStreaming(InputStream inputStream, String fileType, int chunkSize, int overlap, Consumer<String> chunkConsumer) {
        logger.info("开始流式处理文档，文件类型: {}, 分块大小: {}, 重叠: {}", fileType, chunkSize, overlap);

        int totalChunks = 0;

        try {
            switch (fileType.toLowerCase()) {
                case "pdf":
                    totalChunks = processPdfStreaming(inputStream, chunkSize, overlap, chunkConsumer);
                    break;
                case "docx":
                case "doc":
                    totalChunks = processWordStreaming(inputStream, chunkSize, overlap, chunkConsumer);
                    break;
                case "txt":
                    totalChunks = processTxtStreaming(inputStream, chunkSize, overlap, chunkConsumer);
                    break;
                default:
                    throw new IllegalArgumentException("不支持的文件类型: " + fileType);
            }
        } catch (Exception e) {
            logger.error("流式处理文档失败", e);
            throw new RuntimeException("流式处理文档失败: " + e.getMessage(), e);
        }

        logger.info("流式处理文档完成，总分块数: {}", totalChunks);
        return totalChunks;
    }

    @Override
    public int processStreaming(InputStream inputStream, String fileType, Consumer<String> chunkConsumer) {
        return processStreaming(inputStream, fileType, DEFAULT_CHUNK_SIZE, DEFAULT_OVERLAP, chunkConsumer);
    }

    /**
     * 流式处理PDF文档
     * 按页读取，累积文本到缓冲区，达到分块大小时输出
     */
    private int processPdfStreaming(InputStream inputStream, int chunkSize, int overlap, Consumer<String> chunkConsumer) throws Exception {
        int chunkCount = 0;
        StringBuilder buffer = new StringBuilder();

        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);

            int totalPages = document.getNumberOfPages();
            logger.info("PDF总页数: {}", totalPages);

            // 按页读取
            for (int pageNum = 1; pageNum <= totalPages; pageNum++) {
                stripper.setStartPage(pageNum);
                stripper.setEndPage(pageNum);
                String pageText = stripper.getText(document);

                // 清理文本
                pageText = cleanText(pageText);

                if (pageText.isEmpty()) {
                    continue;
                }

                // 将页面文本添加到缓冲区
                buffer.append(pageText).append("\n");

                // 如果缓冲区超过分块大小，进行分块
                while (buffer.length() >= chunkSize) {
                    String chunk = extractChunk(buffer, chunkSize, overlap);
                    if (!chunk.trim().isEmpty()) {
                        chunkConsumer.accept(chunk);
                        chunkCount++;
                        logger.debug("输出分块 #{}, 长度: {}", chunkCount, chunk.length());
                    }
                }
            }

            // 处理剩余的文本
            if (buffer.length() > 0) {
                String remaining = buffer.toString().trim();
                if (!remaining.isEmpty()) {
                    chunkConsumer.accept(remaining);
                    chunkCount++;
                    logger.debug("输出最后分块 #{}, 长度: {}", chunkCount, remaining.length());
                }
            }
        }

        return chunkCount;
    }

    /**
     * 流式处理Word文档
     * 按段落读取，累积文本到缓冲区，达到分块大小时输出
     */
    private int processWordStreaming(InputStream inputStream, int chunkSize, int overlap, Consumer<String> chunkConsumer) throws Exception {
        int chunkCount = 0;
        StringBuilder buffer = new StringBuilder();

        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String paraText = paragraph.getText();

                // 清理文本
                paraText = cleanText(paraText);

                if (paraText.isEmpty()) {
                    continue;
                }

                // 将段落文本添加到缓冲区
                buffer.append(paraText).append("\n");

                // 如果缓冲区超过分块大小，进行分块
                while (buffer.length() >= chunkSize) {
                    String chunk = extractChunk(buffer, chunkSize, overlap);
                    if (!chunk.trim().isEmpty()) {
                        chunkConsumer.accept(chunk);
                        chunkCount++;
                        logger.debug("输出分块 #{}, 长度: {}", chunkCount, chunk.length());
                    }
                }
            }

            // 处理剩余的文本
            if (buffer.length() > 0) {
                String remaining = buffer.toString().trim();
                if (!remaining.isEmpty()) {
                    chunkConsumer.accept(remaining);
                    chunkCount++;
                    logger.debug("输出最后分块 #{}, 长度: {}", chunkCount, remaining.length());
                }
            }
        }

        return chunkCount;
    }

    /**
     * 流式处理TXT文档
     * 按行读取，累积文本到缓冲区，达到分块大小时输出
     */
    private int processTxtStreaming(InputStream inputStream, int chunkSize, int overlap, Consumer<String> chunkConsumer) throws Exception {
        int chunkCount = 0;
        StringBuilder buffer = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // 清理文本
                line = cleanText(line);

                if (line.isEmpty()) {
                    continue;
                }

                // 将行文本添加到缓冲区
                buffer.append(line).append("\n");

                // 如果缓冲区超过分块大小，进行分块
                while (buffer.length() >= chunkSize) {
                    String chunk = extractChunk(buffer, chunkSize, overlap);
                    if (!chunk.trim().isEmpty()) {
                        chunkConsumer.accept(chunk);
                        chunkCount++;
                        logger.debug("输出分块 #{}, 长度: {}", chunkCount, chunk.length());
                    }
                }
            }

            // 处理剩余的文本
            if (buffer.length() > 0) {
                String remaining = buffer.toString().trim();
                if (!remaining.isEmpty()) {
                    chunkConsumer.accept(remaining);
                    chunkCount++;
                    logger.debug("输出最后分块 #{}, 长度: {}", chunkCount, remaining.length());
                }
            }
        }

        return chunkCount;
    }

    /**
     * 从缓冲区提取一个分块
     * 尝试在句子边界处分割，保证语义完整性
     */
    private String extractChunk(StringBuilder buffer, int chunkSize, int overlap) {
        String text = buffer.toString();

        if (text.length() <= chunkSize) {
            buffer.setLength(0);
            return text;
        }

        // 尝试在句子边界处分割
        int splitPos = findSentenceBoundary(text, chunkSize);

        if (splitPos <= 0) {
            // 没有找到合适的句子边界，强制在chunkSize处分割
            splitPos = chunkSize;
        }

        String chunk = text.substring(0, splitPos).trim();

        // 保留overlap字符作为下一块的开始
        int remainingStart = Math.max(0, splitPos - overlap);
        buffer.delete(0, remainingStart);

        return chunk;
    }

    /**
     * 查找句子边界位置
     * 在指定位置附近查找句子结束符（句号、问号、感叹号、换行符等）
     */
    private int findSentenceBoundary(String text, int preferredPosition) {
        // 从preferredPosition向前查找句子边界
        int maxLookBack = Math.min(preferredPosition, 100); // 最多向前查找100个字符

        for (int i = preferredPosition; i > preferredPosition - maxLookBack; i--) {
            if (i >= text.length()) continue;

            char c = text.charAt(i);
            // 检查是否是句子结束符
            if (c == '。' || c == '！' || c == '？' || c == '.' || c == '!' || c == '?' || c == '\n') {
                return i + 1; // 包含结束符
            }
        }

        // 没有找到句子边界，尝试在空格处分割
        for (int i = preferredPosition; i > preferredPosition - maxLookBack; i--) {
            if (i >= text.length()) continue;

            char c = text.charAt(i);
            if (Character.isWhitespace(c)) {
                return i;
            }
        }

        return -1; // 没有找到合适的分割点
    }

    /**
     * 清理文本，去除多余空白和特殊字符
     */
    private String cleanText(String text) {
        if (text == null) {
            return "";
        }
        // 去除多余空白
        return text.replaceAll("\\s+", " ").trim();
    }
}
