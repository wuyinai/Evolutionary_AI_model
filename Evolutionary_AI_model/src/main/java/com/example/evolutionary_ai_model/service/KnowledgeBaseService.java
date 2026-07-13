package com.example.evolutionary_ai_model.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.evolutionary_ai_model.entity.KnowledgeBase;
import com.example.evolutionary_ai_model.entity.KnowledgeDocument;

import java.util.List;

/**
 * 知识库服务接口，负责知识库的创建、查询、删除等操作。
 */
public interface KnowledgeBaseService extends IService<KnowledgeBase> {

    /**
     * 创建知识库
     * @param knowledgeBase 知识库信息
     * @param deptId 创建人所属部门ID（用于建立知识库-部门关联）
     * @return 知识库ID
     */
    Long createKnowledgeBase(KnowledgeBase knowledgeBase, Long deptId);

    /**
     * 获取用户的知识库列表
     * @param userId 用户ID
     * @return 知识库列表
     */
    List<KnowledgeBase> listByUserId(Long userId);

    /**
     * 获取用户可见的知识库列表（用户自己创建的或用户所在部门关联的）
     * @param userId 用户ID
     * @return 知识库列表
     */
    List<KnowledgeBase> listVisibleKnowledgeBases(Long userId);

    /**
     * 获取知识库详情（包含文档列表）
     * @param knowledgeBaseId 知识库ID
     * @return 知识库信息
     */
    KnowledgeBase getKnowledgeBaseDetail(Long knowledgeBaseId);

    /**
     * 更新知识库信息
     * @param knowledgeBase 知识库信息
     */
    void updateKnowledgeBase(KnowledgeBase knowledgeBase);

    /**
     * 删除知识库（包括所有文档）
     * @param knowledgeBaseId 知识库ID
     */
    void deleteKnowledgeBase(Long knowledgeBaseId);

    /**
     * 获取知识库下的文档列表（密级需低于用户角色的最高密级）
     * @param knowledgeBaseId 知识库ID
     * @param userId 用户ID
     * @return 文档列表
     */
    List<KnowledgeDocument> listDocuments(Long knowledgeBaseId, Long userId);

    /**
     * 更新知识库统计信息（文档数量、分块数量）
     * @param knowledgeBaseId 知识库ID
     */
    void updateStatistics(Long knowledgeBaseId);
}