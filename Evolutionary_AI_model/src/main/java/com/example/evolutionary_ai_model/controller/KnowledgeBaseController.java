package com.example.evolutionary_ai_model.controller;

import com.example.evolutionary_ai_model.annotation.OperationLog;
import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.entity.KnowledgeBase;
import com.example.evolutionary_ai_model.entity.KnowledgeDocument;
import com.example.evolutionary_ai_model.entity.SysUserDept;
import com.example.evolutionary_ai_model.mapper.SysUserDeptMapper;
import com.example.evolutionary_ai_model.security.LoginUserDetails;
import com.example.evolutionary_ai_model.service.KnowledgeBaseService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
    private final SysUserDeptMapper sysUserDeptMapper;

    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService, SysUserDeptMapper sysUserDeptMapper) {
        this.knowledgeBaseService = knowledgeBaseService;
        this.sysUserDeptMapper = sysUserDeptMapper;
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
            Long deptId = getDeptId(userDetails);
            knowledgeBase.setUserId(userId);
            Long knowledgeBaseId = knowledgeBaseService.createKnowledgeBase(knowledgeBase, deptId);
            logger.info("知识库创建成功，ID: {}", knowledgeBaseId);
            return Result.success("知识库创建成功", knowledgeBaseId);
        } catch (Exception e) {
            logger.error("创建知识库失败", e);
            return Result.fail("创建知识库失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户可见的知识库列表（用户自己创建的或用户所在部门关联的）
     * 请求地址: GET /knowledge/base/list
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('knowledge:base:list')")
    public Result<List<KnowledgeBase>> listKnowledgeBases(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("获取知识库列表请求");

        try {
            Long userId = getUserId(userDetails);
            List<KnowledgeBase> knowledgeBases = knowledgeBaseService.listVisibleKnowledgeBases(userId);
            logger.info("获取知识库列表成功，数量: {}", knowledgeBases.size());
            return Result.success(knowledgeBases);
        } catch (Exception e) {
            logger.error("获取知识库列表失败", e);
            return Result.fail("获取知识库列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有知识库列表（用于部门管理勾选）
     * 请求地址: GET /knowledge/base/all
     */
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('knowledge:base:list')")
    public Result<List<KnowledgeBase>> listAllKnowledgeBases() {
        logger.info("获取所有知识库列表请求");
        try {
            List<KnowledgeBase> list = knowledgeBaseService.list();
            logger.info("获取所有知识库列表成功，数量: {}", list.size());
            return Result.success(list);
        } catch (Exception e) {
            logger.error("获取所有知识库列表失败", e);
            return Result.fail("获取所有知识库列表失败: " + e.getMessage());
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
     * 获取知识库下的文档列表（密级需低于用户角色的最高密级）
     * 请求地址: GET /knowledge/base/{knowledgeBaseId}/documents
     */
    @GetMapping("/{knowledgeBaseId}/documents")
    @PreAuthorize("hasAuthority('knowledge:base:list')")
    public Result<List<KnowledgeDocument>> listDocuments(@AuthenticationPrincipal UserDetails userDetails,
                                                         @PathVariable Long knowledgeBaseId) {
        logger.info("获取知识库文档列表请求，知识库ID: {}", knowledgeBaseId);

        try {
            Long userId = getUserId(userDetails);
            List<KnowledgeDocument> documents = knowledgeBaseService.listDocuments(knowledgeBaseId, userId);
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

    /**
     * 从UserDetails中获取当前用户所属部门ID（取自用户-部门关联表）
     */
    private Long getDeptId(UserDetails userDetails) {
        if (userDetails instanceof LoginUserDetails) {
            Long userId = ((LoginUserDetails) userDetails).getUserId();
            LambdaQueryWrapper<SysUserDept> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUserDept::getUserId, userId);
            wrapper.last("LIMIT 1");
            SysUserDept userDept = sysUserDeptMapper.selectOne(wrapper);
            return userDept != null ? userDept.getDeptId() : null;
        }
        throw new IllegalArgumentException("无法获取用户信息");
    }

}