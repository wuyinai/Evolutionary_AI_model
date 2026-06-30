package com.example.evolutionary_ai_model.controller;

import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.entity.SysUser;
import com.example.evolutionary_ai_model.entity.vo.ProfileStatsVO;
import com.example.evolutionary_ai_model.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 个人主页控制器
 */
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/user-info")
    public Result<SysUser> getCurrentUserInfo() {
        return profileService.getCurrentUserInfo();
    }

    /**
     * 获取用户统计数据
     */
    @GetMapping("/stats")
    public Result<ProfileStatsVO> getUserStats() {
        return profileService.getUserStats();
    }
}