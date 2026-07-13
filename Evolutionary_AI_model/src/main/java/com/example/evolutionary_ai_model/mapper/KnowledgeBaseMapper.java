package com.example.evolutionary_ai_model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.evolutionary_ai_model.entity.KnowledgeBase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 知识库Mapper接口，负责知识库数据的持久化操作。
 * 位于数据访问层，继承MyBatis-Plus BaseMapper，提供基础CRUD操作。
 */
@Mapper
public interface KnowledgeBaseMapper extends BaseMapper<KnowledgeBase> {

    /**
     * 查询用户可见的知识库列表（用户自己创建的，或对所在部门开放且密级不高于用户最高密级的）
     * @param userId 用户ID
     * @return 知识库列表
     */
    List<KnowledgeBase> selectVisibleKnowledgeBases(@Param("userId") Long userId);
}