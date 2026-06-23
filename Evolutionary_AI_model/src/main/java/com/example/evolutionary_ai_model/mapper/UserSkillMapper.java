package com.example.evolutionary_ai_model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.evolutionary_ai_model.entity.UserSkill;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用法：用户技能Mapper，负责用户技能表的数据访问操作。
 * 继承MyBatis-Plus的BaseMapper，提供基础的CRUD操作。
 */
@Mapper
public interface UserSkillMapper extends BaseMapper<UserSkill> {
}