package com.example.evolutionary_ai_model.service;

import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.entity.SysUser;
import com.example.evolutionary_ai_model.entity.vo.ProfileStatsVO;

/**
 * 个人主页服务接口
 */
public interface ProfileService {

    /**
     * 获取当前登录用户信息
     */
    Result<SysUser> getCurrentUserInfo();

    /**
     * 获取用户统计数据
     */
    Result<ProfileStatsVO> getUserStats();
}