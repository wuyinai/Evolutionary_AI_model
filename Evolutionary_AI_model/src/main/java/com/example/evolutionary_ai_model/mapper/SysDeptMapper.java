package com.example.evolutionary_ai_model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.example.evolutionary_ai_model.entity.SysDept;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用法：部门数据访问层，提供部门的增删改查能力
 */
@Mapper
public interface SysDeptMapper extends BaseMapper<SysDept> {
}
