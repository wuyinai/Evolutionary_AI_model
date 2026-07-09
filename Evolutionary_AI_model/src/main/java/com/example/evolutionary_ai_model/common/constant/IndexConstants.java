package com.example.evolutionary_ai_model.common.constant;

/**
 * 用法：Elasticsearch索引常量类，定义索引名称和字段名称的常量。
 * 位于常量层，提供统一的索引命名规范，避免硬编码。
 * 采用常量模式，集中管理所有索引相关标识。
 */
public class IndexConstants {

    /**
     * 文档索引名称
     */
    public static final String DOCUMENT_INDEX = "evolutionary_ai_documents";

    /**
     * 知识库索引名称
     */
    public static final String KNOWLEDGE_BASE_INDEX = "evolutionary_ai_knowledge_bases";

    /**
     * 对话记录索引名称
     */
    public static final String CONVERSATION_INDEX = "evolutionary_ai_conversations";

    /**
     * 用户操作日志索引名称
     */
    public static final String OPERATION_LOG_INDEX = "evolutionary_ai_operation_logs";

    /**
     * 文档内容索引名称（用于全文搜索）
     */
    public static final String DOCUMENT_CONTENT_INDEX = "evolutionary_ai_document_contents";

    /**
     * 文档索引字段 - 文档ID
     */
    public static final String FIELD_DOCUMENT_ID = "documentId";

    /**
     * 文档索引字段 - 知识库ID
     */
    public static final String FIELD_KNOWLEDGE_BASE_ID = "knowledgeBaseId";

    /**
     * 文档索引字段 - 文档名称
     */
    public static final String FIELD_DOCUMENT_NAME = "documentName";

    /**
     * 文档索引字段 - 文档内容
     */
    public static final String FIELD_DOCUMENT_CONTENT = "documentContent";

    /**
     * 文档索引字段 - 文档类型
     */
    public static final String FIELD_DOCUMENT_TYPE = "documentType";

    /**
     * 文档索引字段 - 创建时间
     */
    public static final String FIELD_CREATE_TIME = "createTime";

    /**
     * 文档索引字段 - 更新时间
     */
    public static final String FIELD_UPDATE_TIME = "updateTime";

    /**
     * 文档索引字段 - 用户ID
     */
    public static final String FIELD_USER_ID = "userId";

    /**
     * 文档索引字段 - 标签
     */
    public static final String FIELD_TAGS = "tags";

    /**
     * 对话索引字段 - 对话ID
     */
    public static final String FIELD_CONVERSATION_ID = "conversationId";

    /**
     * 对话索引字段 - 对话标题
     */
    public static final String FIELD_CONVERSATION_TITLE = "conversationTitle";

    /**
     * 对话索引字段 - 用户问题
     */
    public static final String FIELD_USER_QUESTION = "userQuestion";

    /**
     * 对话索引字段 - AI回答
     */
    public static final String FIELD_AI_RESPONSE = "aiResponse";

    /**
     * 操作日志索引字段 - 操作类型
     */
    public static final String FIELD_OPERATION_TYPE = "operationType";

    /**
     * 操作日志索引字段 - 操作描述
     */
    public static final String FIELD_OPERATION_DESC = "operationDesc";

    /**
     * 操作日志索引字段 - 操作时间
     */
    public static final String FIELD_OPERATION_TIME = "operationTime";

    /**
     * 索引分片数量
     */
    public static final Integer NUMBER_OF_SHARDS = 3;

    /**
     * 索引副本数量
     */
    public static final Integer NUMBER_OF_REPLICAS = 1;

    /**
     * 文档内容字段分片数量（内容较大，需要更多分片）
     */
    public static final Integer DOCUMENT_CONTENT_NUMBER_OF_SHARDS = 5;

    /**
     * 文档内容字段副本数量
     */
    public static final Integer DOCUMENT_CONTENT_NUMBER_OF_REPLICAS = 2;
}