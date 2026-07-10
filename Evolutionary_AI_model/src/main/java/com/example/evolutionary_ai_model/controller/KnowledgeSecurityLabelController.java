package com.example.evolutionary_ai_model.controller;

import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.entity.KnowledgeSecurityLabel;
import com.example.evolutionary_ai_model.mapper.KnowledgeSecurityLabelMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用法：知识库密级标签控制器，提供密级标签的查询接口。
 * 位于表现层，依赖 KnowledgeSecurityLabelMapper 进行数据查询。
 * 供角色管理模块的下拉选择使用。
 */
@Slf4j
@RestController
@RequestMapping("/system/security-label")
@RequiredArgsConstructor
public class KnowledgeSecurityLabelController {

    private final KnowledgeSecurityLabelMapper knowledgeSecurityLabelMapper;

    /**
     * 查询所有密级标签列表
     * 用于角色管理弹窗中的下拉选择
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('sys:security-label:list')")
    public Result<List<KnowledgeSecurityLabel>> listLabels() {
        List<KnowledgeSecurityLabel> labels = knowledgeSecurityLabelMapper.selectList(null);
        log.info("查询密级标签列表成功，数量: {}", labels.size());
        return Result.success(labels);
    }
}
