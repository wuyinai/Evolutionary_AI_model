package com.example.evolutionary_ai_model.controller;

import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.entity.AiConversation;
import com.example.evolutionary_ai_model.entity.dto.ChatRequestDTO;
import com.example.evolutionary_ai_model.entity.vo.ConversationMessageVO;
import com.example.evolutionary_ai_model.security.LoginUserDetails;
import com.example.evolutionary_ai_model.service.AiChatLogService;
import com.example.evolutionary_ai_model.service.AiConversationService;
import com.example.evolutionary_ai_model.service.ChatService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 用法：AI对话Controller，负责接收前端对话请求并返回AI响应。
 * 调用ChatService处理业务逻辑，支持流式对话方式。
 * 位于表现层，只负责接收请求、参数校验、调用业务层、返回响应。
 * 支持动态模型配置，当请求中包含configId时使用DynamicChatStrategy。
 * 从认证信息获取用户ID，实现用户模型配置隔离。
 * 提供对话历史查询接口，支持前端展示聊天记录。
 * 提供会话删除接口，支持逻辑删除会话和消息记录。
 */
@RestController
@RequestMapping("/chat")
public class ChatController {
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final ChatClient chatClient;
    private final ChatService chatService;
    private final AiChatLogService chatLogService;
    private final AiConversationService conversationService;

    public ChatController(ChatClient.Builder chatClientBuilder, ChatService chatService, 
            AiChatLogService chatLogService, AiConversationService conversationService) {
        this.chatClient = chatClientBuilder.build();
        this.chatService = chatService;
        this.chatLogService = chatLogService;
        this.conversationService = conversationService;
    }

    /**
     * 测试接口 - 简单对话（使用配置文件中的默认模型）
     * 请求地址: GET /chat/test
     * 测试数据: message参数，如 "你好"
     */
    @GetMapping("/test")
    public String test(@RequestParam String message) {
        logger.info("测试对话请求，消息: {}", message);
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    /**
     * 流式对话接口 - 支持实时返回AI回复
     * 请求地址: POST /chat/stream
     * 测试数据示例:
     * {
     *   "conversationId": "可选",
     *   "message": "请详细解释量子计算的基本原理",
     *   "configId": 123456789,  // 可选，指定使用的模型配置ID
     *   "history": []
     * }
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@AuthenticationPrincipal UserDetails userDetails,
                               @Valid @RequestBody ChatRequestDTO request) {
        logger.info("流式对话请求，消息长度: {}, configId: {}", 
                request.getMessage().length(), request.getConfigId());

        try {
            // 从认证信息获取用户ID
            Long userId = getUserId(userDetails);
            request.setUserId(userId);

            // 使用动态模型策略进行流式对话
            return chatService.streamChat(request);

        } catch (Exception e) {
            logger.error("流式对话异常", e);
            return Flux.just("错误: AI流式对话失败 - " + e.getMessage());
        }
    }

    /**
     * 从UserDetails获取用户ID
     * @param userDetails 用户详情
     * @return 用户ID，如果未登录返回null
     */
    private Long getUserId(UserDetails userDetails) {
        if (userDetails instanceof LoginUserDetails) {
            return ((LoginUserDetails) userDetails).getUserId();
        }
        logger.warn("无法获取用户ID，userDetails类型: {}", userDetails.getClass().getName());
        return null;
    }

    /**
     * 获取会话的所有消息历史
     * 请求地址: GET /chat/messages/{conversationId}
     * 测试数据: conversationId参数，如 "abc123"
     * 返回数据: 会话消息列表
     */
    @GetMapping("/messages/{conversationId}")
    public Result<List<ConversationMessageVO>> getConversationMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String conversationId) {
        logger.info("获取会话消息历史，会话ID: {}", conversationId);

        try {
            // 验证用户权限（可选）
            Long userId = getUserId(userDetails);

            // 获取会话消息历史
            List<ConversationMessageVO> messages = chatLogService.getConversationMessages(conversationId);

            logger.info("获取会话消息成功，消息数量: {}", messages.size());
            return Result.success(messages);

        } catch (Exception e) {
            logger.error("获取会话消息失败，会话ID: {}", conversationId, e);
            return Result.fail("获取会话消息失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的所有会话列表
     * 请求地址: GET /chat/conversations
     * 返回数据: 会话列表
     * 注意：如果用户未登录（userId为null），返回空列表
     */
    @GetMapping("/conversations")
    public Result<List<AiConversation>> getUserConversations(
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("获取用户会话列表");

        try {
            // 从认证信息获取用户ID
            Long userId = getUserId(userDetails);
            
            // 如果用户未登录，返回空列表（而不是返回错误）
            if (userId == null) {
                logger.info("用户未登录，返回空会话列表");
                return Result.success(List.of());
            }

            // 获取用户会话列表
            List<AiConversation> conversations = chatLogService.getUserConversations(userId);

            logger.info("获取用户会话列表成功，会话数量: {}", conversations.size());
            return Result.success(conversations);

        } catch (Exception e) {
            logger.error("获取用户会话列表失败", e);
            return Result.fail("获取会话列表失败: " + e.getMessage());
        }
    }

    /**
     * 删除会话（逻辑删除）
     * 请求地址: DELETE /chat/conversations/{conversationId}
     * 同时删除该会话的所有消息记录
     */
    @DeleteMapping("/conversations/{conversationId}")
    public Result<Void> deleteConversation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String conversationId) {
        logger.info("删除会话，会话ID: {}", conversationId);

        try {
            // 从认证信息获取用户ID
            Long userId = getUserId(userDetails);
            if (userId == null) {
                return Result.fail("用户未登录");
            }

            // 删除会话（逻辑删除）
            conversationService.deleteConversation(conversationId, userId);

            logger.info("会话删除成功，会话ID: {}", conversationId);
            return Result.success();

        } catch (IllegalArgumentException e) {
            logger.warn("删除会话失败，原因: {}", e.getMessage());
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            logger.error("删除会话失败，会话ID: {}", conversationId, e);
            return Result.fail("删除会话失败: " + e.getMessage());
        }
    }
}