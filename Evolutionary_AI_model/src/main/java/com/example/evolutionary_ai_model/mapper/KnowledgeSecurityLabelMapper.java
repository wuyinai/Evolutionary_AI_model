package com.example.evolutionary_ai_model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.evolutionary_ai_model.entity.KnowledgeSecurityLabel;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用法：知识库密级标签Mapper接口，提供密级标签的数据库访问操作。
 * 位于数据访问层，继承BaseMapper提供默认CRUD实现。
 */
@Mapper
public interface KnowledgeSecurityLabelMapper extends BaseMapper<KnowledgeSecurityLabel> {
}
