package com.example.evolutionary_ai_model.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.evolutionary_ai_model.entity.UserSkill;
import com.example.evolutionary_ai_model.entity.vo.UserSkillVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用法：用户技能服务接口，负责技能包的上传、解压、校验、查询、启用/禁用、删除等操作。
 */
public interface UserSkillService extends IService<UserSkill> {

    /**
     * 上传技能包
     * @param userId 用户ID
     * @param file ZIP文件
     * @return 技能ID
     */
    Long uploadSkill(Long userId, MultipartFile file);

    /**
     * 获取用户的技能列表
     * @param userId 用户ID
     * @return 技能列表
     */
    List<UserSkillVO> listByUserId(Long userId);

    /**
     * 获取技能详情
     * @param skillId 技能ID
     * @return 技能信息
     */
    UserSkillVO getSkillDetail(Long skillId);

    /**
     * 启用/禁用技能
     * @param skillId 技能ID
     * @param enabled 是否启用
     */
    void updateSkillStatus(Long skillId, Boolean enabled);

    /**
     * 删除技能
     * @param skillId 技能ID
     */
    void deleteSkill(Long skillId);

    /**
     * 检查技能名称是否存在
     * @param userId 用户ID
     * @param name 技能名称
     * @return 是否存在
     */
    boolean existsByName(Long userId, String name);
}