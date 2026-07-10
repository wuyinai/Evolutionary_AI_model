package com.example.evolutionary_ai_model.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.entity.SysDict;
import com.example.evolutionary_ai_model.mapper.SysDictMapper;
import com.example.evolutionary_ai_model.service.SysDictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用法：字典管理服务实现类，处理字典的增删改查业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysDictServiceImpl implements SysDictService {

    private final SysDictMapper sysDictMapper;

    @Override
    public Result<Page<SysDict>> listDictTypes(String dictType, String dictName, int page, int size) {
        // 查询所有字典类型（去重）
        List<SysDict> allTypes = sysDictMapper.selectAllDictTypes();

        // 根据搜索条件筛选
        List<SysDict> filteredTypes = allTypes.stream()
                .filter(item -> {
                    boolean match = true;
                    if (dictType != null && !dictType.trim().isEmpty()) {
                        match = item.getDictType() != null && item.getDictType().contains(dictType.trim());
                    }
                    if (dictName != null && !dictName.trim().isEmpty() && match) {
                        match = item.getDictName() != null && item.getDictName().contains(dictName.trim());
                    }
                    return match;
                })
                .collect(java.util.stream.Collectors.toList());

        // 手动分页
        int total = filteredTypes.size();
        int startIndex = (page - 1) * size;
        int endIndex = Math.min(startIndex + size, total);
        List<SysDict> pageRecords = startIndex < total ?
                filteredTypes.subList(startIndex, endIndex) : new java.util.ArrayList<>();

        // 构造Page对象
        Page<SysDict> resultPage = new Page<>(page, size, total);
        resultPage.setRecords(pageRecords);

        log.info("查询字典类型列表，总数: {}, 当前页: {}, 每页大小: {}", total, page, size);
        return Result.success(resultPage);
    }

    @Override
    public Result<List<SysDict>> listDictItemsByType(String dictType) {
        if (dictType == null || dictType.trim().isEmpty()) {
            log.warn("字典类型为空");
            return Result.fail("字典类型不能为空");
        }

        List<SysDict> dictItems = sysDictMapper.selectByDictType(dictType);
        log.info("查询字典项列表，类型: {}, 数量: {}", dictType, dictItems.size());
        return Result.success(dictItems);
    }

    @Override
    public Result<SysDict> getDictById(Long dictId) {
        SysDict dict = sysDictMapper.selectById(dictId);
        if (dict == null || dict.getDelFlag() == 1) {
            log.warn("字典不存在或已删除，ID: {}", dictId);
            return Result.fail("字典不存在");
        }
        log.info("查询字典详情，ID: {}", dictId);
        return Result.success(dict);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> addDictType(SysDict sysDict) {
        // 验证必填字段
        if (sysDict.getDictType() == null || sysDict.getDictType().trim().isEmpty()) {
            log.warn("字典类型为空");
            return Result.fail("字典类型不能为空");
        }
        if (sysDict.getDictName() == null || sysDict.getDictName().trim().isEmpty()) {
            log.warn("字典名称为空");
            return Result.fail("字典名称不能为空");
        }

        // 检查字典类型是否已存在
        LambdaQueryWrapper<SysDict> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysDict::getDictType, sysDict.getDictType().trim());
        queryWrapper.eq(SysDict::getDelFlag, 0);
        Long count = sysDictMapper.selectCount(queryWrapper);
        if (count > 0) {
            log.warn("字典类型已存在，类型: {}", sysDict.getDictType());
            return Result.fail("字典类型已存在");
        }

        sysDict.setDictType(sysDict.getDictType().trim());
        sysDict.setDictName(sysDict.getDictName().trim());
        sysDict.setDelFlag(0);
        sysDict.setStatus(1);
        if (sysDict.getSort() == null) {
            sysDict.setSort(0);
        }

        int result = sysDictMapper.insert(sysDict);
        if (result > 0) {
            log.info("新增字典类型成功，类型: {}, 名称: {}", sysDict.getDictType(), sysDict.getDictName());
            return Result.success();
        } else {
            log.error("新增字典类型失败，类型: {}", sysDict.getDictType());
            return Result.fail("新增字典类型失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> updateDictType(SysDict sysDict) {
        // 验证必填字段
        if (sysDict.getId() == null) {
            log.warn("字典ID为空");
            return Result.fail("字典ID不能为空");
        }
        if (sysDict.getDictName() == null || sysDict.getDictName().trim().isEmpty()) {
            log.warn("字典名称为空");
            return Result.fail("字典名称不能为空");
        }

        // 查询原字典信息
        SysDict existingDict = sysDictMapper.selectById(sysDict.getId());
        if (existingDict == null || existingDict.getDelFlag() == 1) {
            log.warn("字典不存在或已删除，ID: {}", sysDict.getId());
            return Result.fail("字典不存在");
        }

        // 只更新字典名称，不更新字典类型
        sysDict.setDictType(existingDict.getDictType()); // 保持原字典类型不变
        sysDict.setDictName(sysDict.getDictName().trim());
        sysDict.setDelFlag(0);

        int result = sysDictMapper.updateById(sysDict);
        if (result > 0) {
            log.info("修改字典类型成功，类型: {}, 新名称: {}", sysDict.getDictType(), sysDict.getDictName());

            // 同时更新该类型下所有字典项的字典名称
            LambdaQueryWrapper<SysDict> updateWrapper = new LambdaQueryWrapper<>();
            updateWrapper.eq(SysDict::getDictType, existingDict.getDictType());
            updateWrapper.eq(SysDict::getDelFlag, 0);

            SysDict updateDict = new SysDict();
            updateDict.setDictName(sysDict.getDictName());
            sysDictMapper.update(updateDict, updateWrapper);

            return Result.success();
        } else {
            log.error("修改字典类型失败，ID: {}", sysDict.getId());
            return Result.fail("修改字典类型失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteDictType(String dictType) {
        if (dictType == null || dictType.trim().isEmpty()) {
            log.warn("字典类型为空");
            return Result.fail("字典类型不能为空");
        }

        // 查询该类型下的所有字典项
        LambdaQueryWrapper<SysDict> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysDict::getDictType, dictType.trim());
        queryWrapper.eq(SysDict::getDelFlag, 0);
        List<SysDict> dictItems = sysDictMapper.selectList(queryWrapper);

        if (dictItems.isEmpty()) {
            log.warn("字典类型不存在或已删除，类型: {}", dictType);
            return Result.fail("字典类型不存在");
        }

        // 逻辑删除该类型下的所有字典项
        for (SysDict dict : dictItems) {
            dict.setDelFlag(1);
            sysDictMapper.updateById(dict);
        }

        log.info("删除字典类型成功，类型: {}, 删除字典项数量: {}", dictType, dictItems.size());
        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> addDictItem(SysDict sysDict) {
        // 验证必填字段
        if (sysDict.getDictType() == null || sysDict.getDictType().trim().isEmpty()) {
            log.warn("字典类型为空");
            return Result.fail("字典类型不能为空");
        }
        if (sysDict.getDictCode() == null || sysDict.getDictCode().trim().isEmpty()) {
            log.warn("字典编码为空");
            return Result.fail("字典编码不能为空");
        }
        if (sysDict.getDictLabel() == null || sysDict.getDictLabel().trim().isEmpty()) {
            log.warn("字典标签为空");
            return Result.fail("字典标签不能为空");
        }

        // 检查字典编码是否已存在（同一类型下）
        LambdaQueryWrapper<SysDict> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysDict::getDictType, sysDict.getDictType().trim());
        queryWrapper.eq(SysDict::getDictCode, sysDict.getDictCode().trim());
        queryWrapper.eq(SysDict::getDelFlag, 0);
        Long count = sysDictMapper.selectCount(queryWrapper);
        if (count > 0) {
            log.warn("字典编码已存在，类型: {}, 编码: {}", sysDict.getDictType(), sysDict.getDictCode());
            return Result.fail("字典编码已存在");
        }

        sysDict.setDictType(sysDict.getDictType().trim());
        sysDict.setDictCode(sysDict.getDictCode().trim());
        sysDict.setDictLabel(sysDict.getDictLabel().trim());
        sysDict.setDelFlag(0);
        sysDict.setStatus(1);
        if (sysDict.getSort() == null) {
            sysDict.setSort(0);
        }

        int result = sysDictMapper.insert(sysDict);
        if (result > 0) {
            log.info("新增字典项成功，类型: {}, 编码: {}, 标签: {}", sysDict.getDictType(), sysDict.getDictCode(), sysDict.getDictLabel());
            return Result.success();
        } else {
            log.error("新增字典项失败，类型: {}, 编码: {}", sysDict.getDictType(), sysDict.getDictCode());
            return Result.fail("新增字典项失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> updateDictItem(SysDict sysDict) {
        // 验证必填字段
        if (sysDict.getId() == null) {
            log.warn("字典ID为空");
            return Result.fail("字典ID不能为空");
        }
        if (sysDict.getDictLabel() == null || sysDict.getDictLabel().trim().isEmpty()) {
            log.warn("字典标签为空");
            return Result.fail("字典标签不能为空");
        }

        // 查询原字典信息
        SysDict existingDict = sysDictMapper.selectById(sysDict.getId());
        if (existingDict == null || existingDict.getDelFlag() == 1) {
            log.warn("字典不存在或已删除，ID: {}", sysDict.getId());
            return Result.fail("字典不存在");
        }

        sysDict.setDictLabel(sysDict.getDictLabel().trim());
        sysDict.setDelFlag(0);

        int result = sysDictMapper.updateById(sysDict);
        if (result > 0) {
            log.info("修改字典项成功，ID: {}, 新标签: {}", sysDict.getId(), sysDict.getDictLabel());
            return Result.success();
        } else {
            log.error("修改字典项失败，ID: {}", sysDict.getId());
            return Result.fail("修改字典项失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteDictItem(Long dictId) {
        if (dictId == null) {
            log.warn("字典ID为空");
            return Result.fail("字典ID不能为空");
        }

        // 查询字典信息
        SysDict dict = sysDictMapper.selectById(dictId);
        if (dict == null || dict.getDelFlag() == 1) {
            log.warn("字典不存在或已删除，ID: {}", dictId);
            return Result.fail("字典不存在");
        }

        // 逻辑删除
        dict.setDelFlag(1);
        int result = sysDictMapper.updateById(dict);
        if (result > 0) {
            log.info("删除字典项成功，ID: {}, 编码: {}", dictId, dict.getDictCode());
            return Result.success();
        } else {
            log.error("删除字典项失败，ID: {}", dictId);
            return Result.fail("删除字典项失败");
        }
    }
}