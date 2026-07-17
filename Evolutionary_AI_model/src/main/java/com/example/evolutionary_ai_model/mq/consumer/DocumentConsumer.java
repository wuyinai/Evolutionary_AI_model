package com.example.evolutionary_ai_model.mq.consumer;

import com.example.evolutionary_ai_model.common.constant.QueueConstants;
import com.example.evolutionary_ai_model.entity.DocumentChunk;
import com.example.evolutionary_ai_model.entity.KnowledgeBase;
import com.example.evolutionary_ai_model.entity.KnowledgeDocument;
import com.example.evolutionary_ai_model.entity.dto.DocumentProcessMessage;
import com.example.evolutionary_ai_model.mapper.DocumentChunkMapper;
import com.example.evolutionary_ai_model.mq.producer.DocumentProducer;
import com.example.evolutionary_ai_model.service.KnowledgeBaseService;
import com.example.evolutionary_ai_model.service.KnowledgeDocumentService;
import com.example.evolutionary_ai_model.service.MinioService;
import com.example.evolutionary_ai_model.service.StreamingDocumentProcessor;
import com.example.evolutionary_ai_model.service.VectorStoreService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用法：文档处理消息消费者，负责异步处理文档解析、分块、向量化。
 * 位于消息队列层，监听文档处理队列，执行文档处理任务。
 * 采用消费者模式，支持消息确认、失败重试、死信队列处理。
 */
@Component
public class DocumentConsumer {

    private static final Logger logger = LoggerFactory.getLogger(DocumentConsumer.class);

    // 批量处理大小，每处理这么多分块就进行一次向量化
    private static final int BATCH_SIZE = 10;

    @Autowired
    private KnowledgeDocumentService documentService;

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @Autowired
    private MinioService minioService;

    @Autowired
    private StreamingDocumentProcessor streamingProcessor;

    @Autowired
    private VectorStoreService vectorStoreService;

    @Autowired
    private DocumentChunkMapper documentChunkMapper;

    @Autowired
    private DocumentProducer documentProducer;

    /**
     * 监听文档解析队列，处理文档解析、分块、向量化任务
     * 注意：使用手动确认模式，确保消息可靠性
     *
     * @param message 文档处理消息
     * @param channel RabbitMQ通道（用于手动确认消息）
     * @param deliveryTag 消息投递标签（用于ACK/NACK）
     */
    @RabbitListener(queues = QueueConstants.DOCUMENT_PARSE_QUEUE, ackMode = "MANUAL")
    public void handleDocumentProcess(DocumentProcessMessage message,
                                       Channel channel,
                                       @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        Long documentId = message.getDocumentId();
        logger.info("接收到文档处理消息，文档ID: {}, 用户ID: {}, 重试次数: {}",
                documentId, message.getUserId(), message.getRetryCount());

        KnowledgeDocument document = null;
        boolean acked = false; // 标记是否已确认，防止重复确认

        try {
            // 1. 查询文档记录
            document = documentService.getById(documentId);
            if (document == null) {
                logger.error("文档不存在，文档ID: {}", documentId);
                // 消息确认（ACK），避免重复处理不存在的文档
                channel.basicAck(deliveryTag, false);
                acked = true;
                return;
            }

            // 2. 更新状态为处理中
            document.setStatus("PROCESSING");
            documentService.updateById(document);

            // 3. 从MinIO下载文件
            InputStream inputStream = minioService.downloadFile(message.getStoragePath());

            // 4. 流式处理：边解析边分块边向量化
            AtomicInteger chunkIndex = new AtomicInteger(0);
            AtomicInteger totalChunks = new AtomicInteger(0);
            List<DocumentChunk> batch = new ArrayList<>();

            // 使用流式处理器
            int processedChunks = streamingProcessor.processStreaming(
                    inputStream,
                    message.getFileType(),
                    500,  // 分块大小
                    50,   // 重叠大小
                    chunkContent -> {
                        try {
                            int currentIndex = chunkIndex.getAndIncrement();
                            totalChunks.incrementAndGet();

                            // 创建分块记录
                            DocumentChunk chunk = new DocumentChunk();
                            chunk.setDocumentId(documentId);
                            chunk.setKnowledgeBaseId(message.getKnowledgeBaseId());
                            chunk.setUserId(message.getUserId());
                            // 设置分块的密级，继承自文档
                            chunk.setSecurityLabelId(message.getSecurityLabelId());
                            chunk.setChunkIndex(currentIndex);
                            chunk.setContent(chunkContent);

                            // 保存分块记录
                            documentChunkMapper.insert(chunk);

                            // 添加到批处理列表
                            batch.add(chunk);

                            // 达到批处理大小时，进行向量化
                            if (batch.size() >= BATCH_SIZE) {
                                processBatch(batch, message.getEmbeddingModelId());
                                batch.clear();
                            }

                            logger.debug("处理分块 #{}, 长度: {}", currentIndex, chunkContent.length());
                        } catch (Exception e) {
                            logger.error("处理分块失败", e);
                            throw new RuntimeException("处理分块失败: " + e.getMessage(), e);
                        }
                    }
            );

            // 5. 处理剩余的分块
            if (!batch.isEmpty()) {
                processBatch(batch, message.getEmbeddingModelId());
            }

            // 6. 更新文档状态为完成
            document.setStatus("COMPLETED");
            document.setChunkCount(processedChunks);
            documentService.updateById(document);

            logger.info("文档处理完成，文档ID: {}, 总分块数: {}", documentId, processedChunks);

            // 7. 如果文档属于某个知识库，更新知识库的文档数和分块数统计
            if (message.getKnowledgeBaseId() != null) {
                knowledgeBaseService.updateStatistics(message.getKnowledgeBaseId());
            }

            // 8. 消息确认（ACK）- 处理成功
            channel.basicAck(deliveryTag, false);
            acked = true;
            logger.info("文档处理消息确认成功，文档ID: {}", documentId);

        } catch (Exception e) {
            logger.error("文档处理失败，文档ID: {}", documentId, e);

            // 更新文档状态为失败
            if (document != null) {
                try {
                    document.setStatus("FAILED");
                    document.setErrorMessage("文档处理失败: " + e.getMessage());
                    documentService.updateById(document);
                } catch (Exception updateEx) {
                    logger.error("更新文档状态失败", updateEx);
                }
            }

            // 处理失败：发送到延迟队列或死信队列
            handleProcessFailure(message, documentId, e);

            // 消息确认（ACK）- 即使失败也确认，因为已经发送到其他队列
            if (!acked) {
                try {
                    channel.basicAck(deliveryTag, false);
                    acked = true;
                    logger.info("文档处理失败消息已确认，文档ID: {}", documentId);
                } catch (Exception ackEx) {
                    logger.error("消息确认失败", ackEx);
                }
            }
        }
    }

    /**
     * 处理文档处理失败的情况
     * 根据重试次数决定发送到延迟队列还是死信队列
     */
    private void handleProcessFailure(DocumentProcessMessage message, Long documentId, Exception e) {
        try {
            // 判断是否可以重试
            if (message.canRetry()) {
                // 增加重试次数
                message.incrementRetry();

                logger.warn("文档处理失败，准备重试，文档ID: {}, 当前重试次数: {}, 剩余重试次数: {}",
                        documentId, message.getRetryCount(), message.getRemainingRetries());

                // 发送到延迟队列，延迟30秒后重试
                documentProducer.sendDocumentProcessMessageToDelayQueue(message);
            } else {
                logger.error("文档处理失败，已达到最大重试次数，文档ID: {}, 发送到死信队列", documentId);

                // 发送到死信队列
                documentProducer.sendDocumentProcessMessageToDLQ(message);
            }
        } catch (Exception sendEx) {
            logger.error("发送失败消息到队列异常，文档ID: {}", documentId, sendEx);
        }
    }

    /**
     * 监听文档解析死信队列，记录失败消息（可用于人工干预）
     * 注意：使用手动确认模式，确保消息可靠性
     *
     * @param message 文档处理消息
     * @param channel RabbitMQ通道
     * @param deliveryTag 消息投递标签
     */
    @RabbitListener(queues = QueueConstants.DOCUMENT_PARSE_DLQ, ackMode = "MANUAL")
    public void handleDocumentProcessDLQ(DocumentProcessMessage message,
                                          Channel channel,
                                          @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        logger.error("接收到死信队列消息，文档ID: {}, 用户ID: {}, 已重试次数: {}, 文档名: {}",
                message.getDocumentId(), message.getUserId(), message.getRetryCount(), message.getDocumentName());

        boolean acked = false; // 标记是否已确认

        try {
            // TODO: 可以在这里添加人工干预逻辑，例如：
            // 1. 发送通知给管理员
            // 2. 记录到失败任务表
            // 3. 提供手动重试接口

            // 更新文档状态为永久失败
            KnowledgeDocument document = documentService.getById(message.getDocumentId());
            if (document != null) {
                try {
                    document.setStatus("PERMANENTLY_FAILED");
                    document.setErrorMessage("文档处理永久失败，已重试" + message.getRetryCount() + "次");
                    documentService.updateById(document);
                } catch (Exception updateEx) {
                    logger.error("更新文档状态失败", updateEx);
                }
            }

            // 消息确认（ACK）
            channel.basicAck(deliveryTag, false);
            acked = true;

            logger.warn("死信队列消息已处理，文档ID: {}, 需要人工干预", message.getDocumentId());
        } catch (Exception e) {
            logger.error("处理死信队列消息失败，文档ID: {}", message.getDocumentId(), e);

            // 确保消息被确认，避免无限重试
            if (!acked) {
                try {
                    channel.basicAck(deliveryTag, false);
                    logger.info("死信队列消息已确认，文档ID: {}", message.getDocumentId());
                } catch (Exception ackEx) {
                    logger.error("消息确认失败", ackEx);
                }
            }
        }
    }

    /**
     * 批量处理分块：向量化并更新向量ID
     */
    private void processBatch(List<DocumentChunk> batch, Long embeddingModelId) {
        if (batch.isEmpty()) {
            return;
        }

        logger.info("批量向量化，分块数: {}", batch.size());

        // 向量化
        List<String> vectorIds = vectorStoreService.storeVectors(batch, embeddingModelId);

        // 更新向量ID
        for (int i = 0; i < batch.size(); i++) {
            DocumentChunk chunk = batch.get(i);
            chunk.setVectorId(vectorIds.get(i));
            documentChunkMapper.updateById(chunk);
        }

        logger.info("批量向量化完成，向量数: {}", vectorIds.size());
    }
}