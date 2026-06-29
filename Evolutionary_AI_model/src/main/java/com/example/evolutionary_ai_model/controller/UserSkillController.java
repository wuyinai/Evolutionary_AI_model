package com.example.evolutionary_ai_model.controller;

import com.example.evolutionary_ai_model.annotation.OperationLog;
import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.entity.vo.UserSkillVO;
import com.example.evolutionary_ai_model.security.LoginUserDetails;
import com.example.evolutionary_ai_model.service.UserSkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用法：用户技能Controller，负责接收前端技能包管理请求。
 * 提供技能包的上传、查询、启用/禁用、删除等功能。
 */
@Tag(name = "用户技能管理", description = "技能包上传和管理接口")
@RestController
@RequestMapping("/skills")
public class UserSkillController {

    private static final Logger logger = LoggerFactory.getLogger(UserSkillController.class);

    private final UserSkillService userSkillService;

    public UserSkillController(UserSkillService userSkillService) {
        this.userSkillService = userSkillService;
    }

    /**
     * 上传技能包
     * 请求地址: POST /skills/upload
     * 测试数据: multipart/form-data, file参数为ZIP文件
     */
    @Operation(summary = "上传技能包", description = "上传ZIP格式的技能包，自动解压并校验SKILL.md")
    @PostMapping("/upload")
    @OperationLog("上传技能包")
    public Result<Long> uploadSkill(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "ZIP文件") @RequestParam("file") MultipartFile file) {
        logger.info("上传技能包请求，文件名: {}, 大小: {}", file.getOriginalFilename(), file.getSize());

        try {
            Long userId = getUserId(userDetails);
            Long skillId = userSkillService.uploadSkill(userId, file);
            logger.info("技能包上传成功，ID: {}", skillId);
            return Result.success("技能包上传成功", skillId);
        } catch (Exception e) {
            logger.error("上传技能包失败", e);
            return Result.fail("上传技能包失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的技能列表
     * 请求地址: GET /skills/list
     * 测试数据: 无需参数，从token获取用户ID
     */
    @Operation(summary = "获取技能列表", description = "获取当前用户的所有技能包")
    @GetMapping("/list")
    public Result<List<UserSkillVO>> listSkills(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("获取技能列表请求");

        try {
            Long userId = getUserId(userDetails);
            List<UserSkillVO> skills = userSkillService.listByUserId(userId);
            logger.info("获取技能列表成功，数量: {}", skills.size());
            return Result.success(skills);
        } catch (Exception e) {
            logger.error("获取技能列表失败", e);
            return Result.fail("获取技能列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取技能详情
     * 请求地址: GET /skills/{skillId}
     * 测试数据: skillId参数为技能ID
     */
    @Operation(summary = "获取技能详情", description = "根据ID获取技能包详细信息")
    @GetMapping("/{skillId}")
    public Result<UserSkillVO> getSkillDetail(@PathVariable Long skillId) {
        logger.info("获取技能详情请求，ID: {}", skillId);

        try {
            UserSkillVO skill = userSkillService.getSkillDetail(skillId);
            if (skill == null) {
                return Result.fail("技能不存在");
            }
            return Result.success(skill);
        } catch (Exception e) {
            logger.error("获取技能详情失败", e);
            return Result.fail("获取技能详情失败: " + e.getMessage());
        }
    }

    /**
     * 启用/禁用技能
     * 请求地址: PUT /skills/{skillId}/status
     * 测试数据: enabled参数为true或false
     */
    @Operation(summary = "更新技能状态", description = "启用或禁用技能包")
    @PutMapping("/{skillId}/status")
    @OperationLog("更新技能状态")
    public Result<Void> updateSkillStatus(
            @PathVariable Long skillId,
            @Parameter(description = "是否启用") @RequestParam Boolean enabled) {
        logger.info("更新技能状态请求，ID: {}, 启用: {}", skillId, enabled);

        try {
            userSkillService.updateSkillStatus(skillId, enabled);
            logger.info("技能状态更新成功");
            return Result.success();
        } catch (Exception e) {
            logger.error("更新技能状态失败", e);
            return Result.fail("更新技能状态失败: " + e.getMessage());
        }
    }

    /**
     * 删除技能
     * 请求地址: DELETE /skills/{skillId}
     * 测试数据: skillId参数为技能ID
     */
    @Operation(summary = "删除技能", description = "删除技能包及其文件")
    @DeleteMapping("/{skillId}")
    @OperationLog("删除技能")
    public Result<Void> deleteSkill(@PathVariable Long skillId) {
        logger.info("删除技能请求，ID: {}", skillId);

        try {
            userSkillService.deleteSkill(skillId);
            logger.info("技能删除成功");
            return Result.success();
        } catch (Exception e) {
            logger.error("删除技能失败", e);
            return Result.fail("删除技能失败: " + e.getMessage());
        }
    }

    /**
     * 从UserDetails中获取用户ID
     */
    private Long getUserId(UserDetails userDetails) {
        if (userDetails instanceof LoginUserDetails) {
            return ((LoginUserDetails) userDetails).getUserId();
        }
        throw new IllegalArgumentException("无法获取用户信息");
    }
}