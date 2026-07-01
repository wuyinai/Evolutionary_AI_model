package com.example.evolutionary_ai_model.controller;

import com.example.evolutionary_ai_model.annotation.OperationLog;
import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.entity.KnowledgeBase;
import com.example.evolutionary_ai_model.entity.KnowledgeDocument;
import com.example.evolutionary_ai_model.security.LoginUserDetails;
import com.example.evolutionary_ai_model.service.KnowledgeBaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 知识库Controller，负责接收前端知识库管理请求。
 * 提供知识库的创建、查询、更新、删除等功能。
 */
@RestController
@RequestMapping("/knowledge/base")
public class KnowledgeBaseController {

    private static final Logger logger = LoggerFactory.getLogger(KnowledgeBaseController.class);

    private final KnowledgeBaseService knowledgeBaseService;

    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    /**
     * 创建知识库
     * 请求地址: POST /knowledge/base
     */
    @PostMapping
    @PreAuthorize("hasAuthority('knowledge:base:add')")
    @OperationLog("创建知识库")
    public Result<Long> createKnowledgeBase(@AuthenticationPrincipal UserDetails userDetails,
                                            @RequestBody KnowledgeBase knowledgeBase) {
        logger.info("创建知识库请求，名称: {}", knowledgeBase.getName());

        try {
            Long userId = getUserId(userDetails);
            knowledgeBase.setUserId(userId);
            Long knowledgeBaseId = knowledgeBaseService.createKnowledgeBase(knowledgeBase);
            logger.info("知识库创建成功，ID: {}", knowledgeBaseId);
            return Result.success("知识库创建成功", knowledgeBaseId);
        } catch (Exception e) {
            logger.error("创建知识库失败", e);
            return Result.fail("创建知识库失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的知识库列表
     * 请求地址: GET /knowledge/base/list
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('knowledge:base:list')")
    public Result<List<KnowledgeBase>> listKnowledgeBases(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("获取知识库列表请求");

        try {
            Long userId = getUserId(userDetails);
            List<KnowledgeBase> knowledgeBases = knowledgeBaseService.listByUserId(userId);
            logger.info("获取知识库列表成功，数量: {}", knowledgeBases.size());
            return Result.success(knowledgeBases);
        } catch (Exception e) {
            logger.error("获取知识库列表失败", e);
            return Result.fail("获取知识库列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取知识库详情
     * 请求地址: GET /knowledge/base/{knowledgeBaseId}
     */
    @GetMapping("/{knowledgeBaseId}")
    @PreAuthorize("hasAuthority('knowledge:base:list')")
    public Result<KnowledgeBase> getKnowledgeBase(@PathVariable Long knowledgeBaseId) {
        logger.info("获取知识库详情请求，ID: {}", knowledgeBaseId);

        try {
            KnowledgeBase knowledgeBase = knowledgeBaseService.getKnowledgeBaseDetail(knowledgeBaseId);
            if (knowledgeBase == null) {
                return Result.fail("知识库不存在");
            }
            return Result.success(knowledgeBase);
        } catch (Exception e) {
            logger.error("获取知识库详情失败", e);
            return Result.fail("获取知识库详情失败: " + e.getMessage());
        }
    }

    /**
     * 更新知识库信息
     * 请求地址: PUT /knowledge/base
     */
    @PutMapping
    @PreAuthorize("hasAuthority('knowledge:base:edit')")
    @OperationLog("更新知识库")
    public Result<Void> updateKnowledgeBase(@RequestBody KnowledgeBase knowledgeBase) {
        logger.info("更新知识库请求，ID: {}", knowledgeBase.getId());

        try {
            knowledgeBaseService.updateKnowledgeBase(knowledgeBase);
            logger.info("知识库更新成功，ID: {}", knowledgeBase.getId());
            return Result.success();
        } catch (Exception e) {
            logger.error("更新知识库失败", e);
            return Result.fail("更新知识库失败: " + e.getMessage());
        }
    }

    /**
     * 删除知识库
     * 请求地址: DELETE /knowledge/base/{knowledgeBaseId}
     */
    @DeleteMapping("/{knowledgeBaseId}")
    @PreAuthorize("hasAuthority('knowledge:base:delete')")
    @OperationLog("删除知识库")
    public Result<Void> deleteKnowledgeBase(@PathVariable Long knowledgeBaseId) {
        logger.info("删除知识库请求，ID: {}", knowledgeBaseId);

        try {
            knowledgeBaseService.deleteKnowledgeBase(knowledgeBaseId);
            logger.info("知识库删除成功，ID: {}", knowledgeBaseId);
            return Result.success();
        } catch (Exception e) {
            logger.error("删除知识库失败", e);
            return Result.fail("删除知识库失败: " + e.getMessage());
        }
    }

    /**
     * 获取知识库下的文档列表
     * 请求地址: GET /knowledge/base/{knowledgeBaseId}/documents
     */
    @GetMapping("/{knowledgeBaseId}/documents")
    @PreAuthorize("hasAuthority('knowledge:base:list')")
    public Result<List<KnowledgeDocument>> listDocuments(@PathVariable Long knowledgeBaseId) {
        logger.info("获取知识库文档列表请求，知识库ID: {}", knowledgeBaseId);

        try {
            List<KnowledgeDocument> documents = knowledgeBaseService.listDocuments(knowledgeBaseId);
            logger.info("获取文档列表成功，数量: {}", documents.size());
            return Result.success(documents);
        } catch (Exception e) {
            logger.error("获取文档列表失败", e);
            return Result.fail("获取文档列表失败: " + e.getMessage());
        }
    }

    /**
     * 从UserDetails中获取用户ID
     */
    private Long getUserId(UserDetails userDetails) {
        if (userDetails instanceof LoginUserDetails) {
            return ((LoginUserDetails) userDetails).getUserId();
        }
        throw new IllegalArgumentException("无法获取用户信息");
    }
}