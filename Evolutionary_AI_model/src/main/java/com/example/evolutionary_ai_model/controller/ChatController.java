package com.example.evolutionary_ai_model.controller;
 
 
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
 
/**
 * @author txx
 * @version [1.0]
 * @description
 * @date 2025/10/24 10:14
 */
 
@RestController
@RequestMapping("/chat")
public class ChatController {
    private ChatClient chatClient;
 
    // 构造函数,依赖注入ChatClient，spring会自动将配置文件中的模型相关信息进行注入
    public ChatController(ChatClient.Builder chatClientBuilder){
        this.chatClient = chatClientBuilder.build();
    }
 
    @GetMapping("/test")   //方法级别的请求路径
    public String test(@RequestParam  String message){  //参数为需要接收的消息
        return chatClient.prompt()  // 使用 chatClient 开始构建一个对 AI 的请求（即一个"提示" Prompt）
        .user( message)  // 将消息传入
        .call()  // 调用 AI
        .content();     // 获取 AI 的回复内容
    }
}