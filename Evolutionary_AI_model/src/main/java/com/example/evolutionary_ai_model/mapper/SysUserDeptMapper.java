package com.example.evolutionary_ai_model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.evolutionary_ai_model.entity.SysUserDept;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户部门关联 Mapper 接口
 */
@Mapper
public interface SysUserDeptMapper extends BaseMapper<SysUserDept> {
}
