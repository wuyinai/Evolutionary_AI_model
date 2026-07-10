package com.example.evolutionary_ai_model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.evolutionary_ai_model.entity.KnowledgeBaseDept;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识库与部门关联 Mapper 接口
 */
@Mapper
public interface KnowledgeBaseDeptMapper extends BaseMapper<KnowledgeBaseDept> {
}
