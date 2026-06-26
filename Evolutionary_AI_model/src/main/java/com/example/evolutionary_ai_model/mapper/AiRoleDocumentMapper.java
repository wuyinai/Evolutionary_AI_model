package com.example.evolutionary_ai_model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.evolutionary_ai_model.entity.AiRoleDocument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用法：AI角色文档Mapper接口，负责角色关联文档数据的持久化操作。
 * 位于数据访问层，继承MyBatis-Plus BaseMapper，提供基础CRUD操作。
 * 扩展deleteByRoleId方法用于删除角色时批量删除关联文档。
 */
@Mapper
public interface AiRoleDocumentMapper extends BaseMapper<AiRoleDocument> {

    /**
     * 根据角色ID删除所有关联文档
     * @param roleId 角色ID
     * @return 删除的文档数量
     */
    int deleteByRoleId(@Param("roleId") Long roleId);
}