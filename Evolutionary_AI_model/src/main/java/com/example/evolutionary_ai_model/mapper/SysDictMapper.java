package com.example.evolutionary_ai_model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.evolutionary_ai_model.entity.SysDict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用法：字典Mapper接口，负责字典数据的持久化操作
 * 提供字典表的增删改查方法
 */
@Mapper
public interface SysDictMapper extends BaseMapper<SysDict> {

    /**
     * 根据字典类型查询字典列表
     *
     * @param dictType 字典类型
     * @return 字典列表
     */
    List<SysDict> selectByDictType(@Param("dictType") String dictType);

    /**
     * 查询所有字典类型（去重）
     *
     * @return 字典类型列表
     */
    List<SysDict> selectAllDictTypes();
}