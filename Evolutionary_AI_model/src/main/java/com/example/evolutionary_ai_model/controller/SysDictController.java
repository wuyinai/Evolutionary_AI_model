package com.example.evolutionary_ai_model.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.entity.SysDict;
import com.example.evolutionary_ai_model.service.SysDictService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用法：字典管理控制器，提供字典增删改查的REST接口，需要相应权限才能访问
 */
@RestController
@RequestMapping("/system/dict")
@RequiredArgsConstructor
public class SysDictController {

    // 字典管理服务，处理字典增删改查业务逻辑
    private final SysDictService sysDictService;

    /**
     * 分页查询字典类型列表
     */
    @GetMapping("/types/list")
    @PreAuthorize("hasAuthority('sys:dict:list')")
    public Result<Page<SysDict>> listDictTypes(
            @RequestParam(required = false) String dictType,
            @RequestParam(required = false) String dictName,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return sysDictService.listDictTypes(dictType, dictName, page, size);
    }

    /**
     * 根据字典类型查询字典项列表
     */
    @GetMapping("/items/{dictType}")
    @PreAuthorize("hasAuthority('sys:dict:list')")
    public Result<List<SysDict>> listDictItemsByType(@PathVariable String dictType) {
        return sysDictService.listDictItemsByType(dictType);
    }

    /**
     * 根据ID查询字典详情
     */
    @GetMapping("/{dictId}")
    @PreAuthorize("hasAuthority('sys:dict:list')")
    public Result<SysDict> getDictById(@PathVariable Long dictId) {
        return sysDictService.getDictById(dictId);
    }

    /**
     * 新增字典类型
     */
    @PostMapping("/type")
    @PreAuthorize("hasAuthority('sys:dict:add')")
    public Result<Void> addDictType(@RequestBody SysDict sysDict) {
        return sysDictService.addDictType(sysDict);
    }

    /**
     * 修改字典类型
     */
    @PutMapping("/type")
    @PreAuthorize("hasAuthority('sys:dict:edit')")
    public Result<Void> updateDictType(@RequestBody SysDict sysDict) {
        return sysDictService.updateDictType(sysDict);
    }

    /**
     * 删除字典类型
     */
    @DeleteMapping("/type/{dictType}")
    @PreAuthorize("hasAuthority('sys:dict:delete')")
    public Result<Void> deleteDictType(@PathVariable String dictType) {
        return sysDictService.deleteDictType(dictType);
    }

    /**
     * 新增字典项
     */
    @PostMapping("/item")
    @PreAuthorize("hasAuthority('sys:dict:add')")
    public Result<Void> addDictItem(@RequestBody SysDict sysDict) {
        return sysDictService.addDictItem(sysDict);
    }

    /**
     * 修改字典项
     */
    @PutMapping("/item")
    @PreAuthorize("hasAuthority('sys:dict:edit')")
    public Result<Void> updateDictItem(@RequestBody SysDict sysDict) {
        return sysDictService.updateDictItem(sysDict);
    }

    /**
     * 删除字典项
     */
    @DeleteMapping("/item/{dictId}")
    @PreAuthorize("hasAuthority('sys:dict:delete')")
    public Result<Void> deleteDictItem(@PathVariable Long dictId) {
        return sysDictService.deleteDictItem(dictId);
    }
}