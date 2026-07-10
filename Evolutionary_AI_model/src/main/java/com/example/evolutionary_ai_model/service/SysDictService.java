package com.example.evolutionary_ai_model.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.entity.SysDict;

import java.util.List;

/**
 * 用法：字典管理服务接口，定义字典的增删改查业务方法
 */
public interface SysDictService {

    /**
     * 分页查询字典类型列表
     *
     * @param dictType 字典类型（模糊查询）
     * @param dictName 字典名称（模糊查询）
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    Result<Page<SysDict>> listDictTypes(String dictType, String dictName, int page, int size);

    /**
     * 根据字典类型查询字典项列表
     *
     * @param dictType 字典类型
     * @return 字典项列表
     */
    Result<List<SysDict>> listDictItemsByType(String dictType);

    /**
     * 根据ID查询字典详情
     *
     * @param dictId 字典ID
     * @return 字典详情
     */
    Result<SysDict> getDictById(Long dictId);

    /**
     * 新增字典类型
     *
     * @param sysDict 字典信息
     * @return 操作结果
     */
    Result<Void> addDictType(SysDict sysDict);

    /**
     * 修改字典类型
     *
     * @param sysDict 字典信息
     * @return 操作结果
     */
    Result<Void> updateDictType(SysDict sysDict);

    /**
     * 删除字典类型（同时删除该类型下的所有字典项）
     *
     * @param dictType 字典类型
     * @return 操作结果
     */
    Result<Void> deleteDictType(String dictType);

    /**
     * 新增字典项
     *
     * @param sysDict 字典项信息
     * @return 操作结果
     */
    Result<Void> addDictItem(SysDict sysDict);

    /**
     * 修改字典项
     *
     * @param sysDict 字典项信息
     * @return 操作结果
     */
    Result<Void> updateDictItem(SysDict sysDict);

    /**
     * 删除字典项
     *
     * @param dictId 字典项ID
     * @return 操作结果
     */
    Result<Void> deleteDictItem(Long dictId);
}