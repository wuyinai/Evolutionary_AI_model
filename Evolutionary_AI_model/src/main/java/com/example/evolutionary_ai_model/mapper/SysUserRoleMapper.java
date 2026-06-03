package com.example.evolutionary_ai_model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.evolutionary_ai_model.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用法：用户角色关联数据访问层，提供用户角色的增删改查能力
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
}
