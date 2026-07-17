package com.example.evolutionary_ai_model.mq.producer;

import com.example.evolutionary_ai_model.common.constant.QueueConstants;
import com.example.evolutionary_ai_model.entity.dto.DocumentProcessMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用法：文档处理消息生产者，负责将文档处理任务发送到RabbitMQ队列。
 * 位于消息队列层，提供消息发送的统一入口。
 * 采用生产者模式，封装消息发送逻辑，支持消息确认和重试。
 */
@Component
public class DocumentProducer {

    private static final Logger logger = LoggerFactory.getLogger(DocumentProducer.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送文档处理消息到队列
     *
     * @param message 文档处理消息
     */
    public void sendDocumentProcessMessage(DocumentProcessMessage message) {
        try {
            logger.info("发送文档处理消息，文档ID: {}, 用户ID: {}, 文档名: {}, 重试次数: {}",
                    message.getDocumentId(), message.getUserId(), message.getDocumentName(), message.getRetryCount());

            // 发送消息到文档处理交换机，使用 document.parse 路由键
            rabbitTemplate.convertAndSend(
                    QueueConstants.DOCUMENT_PROCESS_EXCHANGE,
                    QueueConstants.DOCUMENT_PARSE_ROUTING_KEY,
                    message
            );

            logger.info("文档处理消息发送成功，文档ID: {}", message.getDocumentId());
        } catch (Exception e) {
            logger.error("文档处理消息发送失败，文档ID: {}", message.getDocumentId(), e);
            throw new RuntimeException("文档处理消息发送失败: " + e.getMessage(), e);
        }
    }

    /**
     * 发送文档处理消息到延迟队列（用于重试）
     *
     * @param message 文档处理消息
     */
    public void sendDocumentProcessMessageToDelayQueue(DocumentProcessMessage message) {
        try {
            logger.info("发送文档处理消息到延迟队列，文档ID: {}, 延迟时间: {}ms",
                    message.getDocumentId(), QueueConstants.DOCUMENT_PARSE_DELAY);

            // 发送消息到死信交换机，路由到延迟队列
            // 延迟队列中的消息过期后，会自动路由回文档处理交换机
            rabbitTemplate.convertAndSend(
                    QueueConstants.DEAD_LETTER_EXCHANGE,
                    QueueConstants.DOCUMENT_PARSE_ROUTING_KEY,
                    message
            );

            logger.info("文档处理延迟消息发送成功，文档ID: {}", message.getDocumentId());
        } catch (Exception e) {
            logger.error("文档处理延迟消息发送失败，文档ID: {}", message.getDocumentId(), e);
            throw new RuntimeException("文档处理延迟消息发送失败: " + e.getMessage(), e);
        }
    }

    /**
     * 发送文档处理失败消息到死信队列
     *
     * @param message 文档处理消息
     */
    public void sendDocumentProcessMessageToDLQ(DocumentProcessMessage message) {
        try {
            logger.warn("发送文档处理消息到死信队列，文档ID: {}, 已重试次数: {}",
                    message.getDocumentId(), message.getRetryCount());

            // 发送消息到死信交换机，路由到死信队列
            rabbitTemplate.convertAndSend(
                    QueueConstants.DEAD_LETTER_EXCHANGE,
                    QueueConstants.DOCUMENT_PARSE_DLQ_ROUTING_KEY,
                    message
            );

            logger.warn("文档处理失败消息已发送到死信队列，文档ID: {}", message.getDocumentId());
        } catch (Exception e) {
            logger.error("文档处理失败消息发送到死信队列失败，文档ID: {}", message.getDocumentId(), e);
            throw new RuntimeException("文档处理失败消息发送失败: " + e.getMessage(), e);
        }
    }
}