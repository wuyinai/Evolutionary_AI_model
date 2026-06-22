package com.example.evolutionary_ai_model.entity.dto;

import java.io.Serializable;

/**
 * 文档块信息DTO
 * 用于RAG检索结果展示，包含文档块的详细信息和来源
 */
public class DocumentChunkDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文档块ID（向量存储中的唯一标识）
     */
    private String chunkId;

    /**
     * 文档块内容
     */
    private String content;

    /**
     * 来源文档ID
     */
    private Long documentId;

    /**
     * 来源文档名称
     */
    private String documentName;

    /**
     * 文档块在原文中的位置（可选）
     */
    private Integer chunkIndex;

    /**
     * 相似度得分（0-1之间，越接近1越相似）
     */
    private Double similarityScore;

    /**
     * 文档块摘要（用于折叠展示时的标题）
     * 如果内容超过50字符，自动截取前50字符
     */
    private String summary;

    public DocumentChunkDTO() {
    }

    public DocumentChunkDTO(String chunkId, String content, Long documentId, String documentName) {
        this.chunkId = chunkId;
        this.content = content;
        this.documentId = documentId;
        this.documentName = documentName;
        this.summary = generateSummary(content);
    }

    public DocumentChunkDTO(String chunkId, String content, Long documentId, String documentName,
                            Integer chunkIndex, Double similarityScore) {
        this.chunkId = chunkId;
        this.content = content;
        this.documentId = documentId;
        this.documentName = documentName;
        this.chunkIndex = chunkIndex;
        this.similarityScore = similarityScore;
        this.summary = generateSummary(content);
    }

    /**
     * 生成文档块摘要
     * @param content 文档块内容
     * @return 摘要文本
     */
    private String generateSummary(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        // 去除换行符和多余空格
        String cleaned = content.replaceAll("\\s+", " ").trim();
        // 截取前50字符作为摘要
        if (cleaned.length() > 50) {
            return cleaned.substring(0, 50) + "...";
        }
        return cleaned;
    }

    // Getters and Setters

    public String getChunkId() {
        return chunkId;
    }

    public void setChunkId(String chunkId) {
        this.chunkId = chunkId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        this.summary = generateSummary(content);
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public Integer getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(Integer chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public Double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(Double similarityScore) {
        this.similarityScore = similarityScore;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    @Override
    public String toString() {
        return "DocumentChunkDTO{" +
                "chunkId='" + chunkId + '\'' +
                ", documentId=" + documentId +
                ", documentName='" + documentName + '\'' +
                ", chunkIndex=" + chunkIndex +
                ", similarityScore=" + similarityScore +
                ", summary='" + summary + '\'' +
                '}';
    }
}
