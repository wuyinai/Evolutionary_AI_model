-- 为ai_conversation_message表添加document_chunks字段，用于存储知识库文档块信息
-- 执行时间：2026-06-22

ALTER TABLE `ai_conversation_message` 
ADD COLUMN `document_chunks` TEXT DEFAULT NULL COMMENT '知识库文档块信息（JSON格式，存储引用的文档块列表）' 
AFTER `content`;