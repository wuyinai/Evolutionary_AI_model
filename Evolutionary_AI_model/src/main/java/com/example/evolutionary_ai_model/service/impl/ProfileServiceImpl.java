package com.example.evolutionary_ai_model.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.evolutionary_ai_model.common.result.Result;
import com.example.evolutionary_ai_model.entity.AiConversation;
import com.example.evolutionary_ai_model.entity.AiConversationMessage;
import com.example.evolutionary_ai_model.entity.AiChatLog;
import com.example.evolutionary_ai_model.entity.SysUser;
import com.example.evolutionary_ai_model.entity.vo.ProfileStatsVO;
import com.example.evolutionary_ai_model.entity.vo.ConversationStatsVO;
import com.example.evolutionary_ai_model.entity.vo.TokenStatsVO;
import com.example.evolutionary_ai_model.entity.vo.DailyTokenUsageVO;
import com.example.evolutionary_ai_model.mapper.SysUserMapper;
import com.example.evolutionary_ai_model.mapper.AiConversationMapper;
import com.example.evolutionary_ai_model.mapper.AiConversationMessageMapper;
import com.example.evolutionary_ai_model.mapper.AiChatLogMapper;
import com.example.evolutionary_ai_model.security.LoginUserDetails;
import com.example.evolutionary_ai_model.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 个人主页服务实现类
 */
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileServiceImpl.class);

    private final SysUserMapper sysUserMapper;
    private final AiConversationMapper conversationMapper;
    private final AiConversationMessageMapper conversationMessageMapper;
    private final AiChatLogMapper chatLogMapper;

    @Override
    public Result<SysUser> getCurrentUserInfo() {
        try {
            // 获取当前登录用户ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return Result.fail("用户未登录");
            }

            LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
            Long userId = userDetails.getUserId();

            logger.info("获取当前用户信息，用户ID: {}", userId);

            // 查询用户完整信息
            SysUser sysUser = sysUserMapper.selectById(userId);
            if (sysUser == null) {
                return Result.fail("用户不存在");
            }

            // 清除密码信息，不返回给前端
            sysUser.setPassword(null);

            logger.info("用户信息获取成功，用户ID: {}", userId);
            return Result.success(sysUser);

        } catch (Exception e) {
            logger.error("获取用户信息失败", e);
            return Result.fail("获取用户信息失败: " + e.getMessage());
        }
    }

    @Override
    public Result<ProfileStatsVO> getUserStats() {
        try {
            // 获取当前登录用户ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return Result.fail("用户未登录");
            }

            LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
            Long userId = userDetails.getUserId();

            logger.info("获取用户统计数据，用户ID: {}", userId);

            // 构建统计数据对象
            ProfileStatsVO statsVO = new ProfileStatsVO();

            // 获取对话统计
            ConversationStatsVO conversationStats = getConversationStats(userId);
            statsVO.setConversationStats(conversationStats);

            // 获取Token统计
            TokenStatsVO tokenStats = getTokenStats(userId);
            statsVO.setTokenStats(tokenStats);

            logger.info("用户统计数据获取成功，用户ID: {}", userId);
            return Result.success(statsVO);

        } catch (Exception e) {
            logger.error("获取用户统计数据失败", e);
            return Result.fail("获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取对话统计数据
     */
    private ConversationStatsVO getConversationStats(Long userId) {
        ConversationStatsVO stats = new ConversationStatsVO();

        // 总对话次数
        LambdaQueryWrapper<AiConversation> totalWrapper = new LambdaQueryWrapper<>();
        totalWrapper.eq(AiConversation::getUserId, userId);
        Long totalCount = conversationMapper.selectCount(totalWrapper);
        stats.setTotalCount(totalCount.intValue());

        // 今日对话次数
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        LambdaQueryWrapper<AiConversation> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.eq(AiConversation::getUserId, userId)
                .between(AiConversation::getCreateTime, todayStart, todayEnd);
        Long todayCount = conversationMapper.selectCount(todayWrapper);
        stats.setTodayCount(todayCount.intValue());

        // 总消息数
        LambdaQueryWrapper<AiConversation> conversationIdsWrapper = new LambdaQueryWrapper<>();
        conversationIdsWrapper.eq(AiConversation::getUserId, userId)
                .select(AiConversation::getConversationId);
        List<AiConversation> conversations = conversationMapper.selectList(conversationIdsWrapper);

        if (!conversations.isEmpty()) {
            List<String> conversationIds = conversations.stream()
                    .map(AiConversation::getConversationId)
                    .toList();

            LambdaQueryWrapper<AiConversationMessage> messageWrapper = new LambdaQueryWrapper<>();
            messageWrapper.in(AiConversationMessage::getConversationId, conversationIds);
            Long totalMessages = conversationMessageMapper.selectCount(messageWrapper);
            stats.setTotalMessages(totalMessages.intValue());
        } else {
            stats.setTotalMessages(0);
        }

        // 活跃对话数（状态为1的对话）
        LambdaQueryWrapper<AiConversation> activeWrapper = new LambdaQueryWrapper<>();
        activeWrapper.eq(AiConversation::getUserId, userId)
                .eq(AiConversation::getStatus, 1);
        Long activeCount = conversationMapper.selectCount(activeWrapper);
        stats.setActiveCount(activeCount.intValue());

        return stats;
    }

    /**
     * 获取Token统计数据
     */
    private TokenStatsVO getTokenStats(Long userId) {
        TokenStatsVO stats = new TokenStatsVO();

        // 查询该用户所有的聊天日志
        LambdaQueryWrapper<AiChatLog> logWrapper = new LambdaQueryWrapper<>();
        logWrapper.eq(AiChatLog::getUserId, userId);
        List<AiChatLog> chatLogs = chatLogMapper.selectList(logWrapper);

        // 总Token消耗
        long totalTokens = chatLogs.stream()
                .mapToLong(log -> log.getTotalTokens() != null ? log.getTotalTokens() : 0)
                .sum();
        stats.setTotalTokens(totalTokens);

        // 输入Token数
        long inputTokens = chatLogs.stream()
                .mapToLong(log -> log.getRequestTokens() != null ? log.getRequestTokens() : 0)
                .sum();
        stats.setInputTokens(inputTokens);

        // 输出Token数
        long outputTokens = chatLogs.stream()
                .mapToLong(log -> log.getResponseTokens() != null ? log.getResponseTokens() : 0)
                .sum();
        stats.setOutputTokens(outputTokens);

        // 今日Token消耗
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        LambdaQueryWrapper<AiChatLog> todayLogWrapper = new LambdaQueryWrapper<>();
        todayLogWrapper.eq(AiChatLog::getUserId, userId)
                .between(AiChatLog::getRequestTime, todayStart, todayEnd);
        List<AiChatLog> todayLogs = chatLogMapper.selectList(todayLogWrapper);
        long todayTokens = todayLogs.stream()
                .mapToLong(log -> log.getTotalTokens() != null ? log.getTotalTokens() : 0)
                .sum();
        stats.setTodayTokens(todayTokens);

        // 近7天Token使用趋势
        List<DailyTokenUsageVO> weeklyTrend = getWeeklyTokenTrend(userId);
        stats.setWeeklyTrend(weeklyTrend);

        return stats;
    }

    /**
     * 获取近7天Token使用趋势
     */
    private List<DailyTokenUsageVO> getWeeklyTokenTrend(Long userId) {
        List<DailyTokenUsageVO> trend = new ArrayList<>();

        // 获取最近7天的日期
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);

            // 查询该天的Token使用量
            LambdaQueryWrapper<AiChatLog> dayLogWrapper = new LambdaQueryWrapper<>();
            dayLogWrapper.eq(AiChatLog::getUserId, userId)
                    .between(AiChatLog::getRequestTime, dayStart, dayEnd);
            List<AiChatLog> dayLogs = chatLogMapper.selectList(dayLogWrapper);

            long dayTokens = dayLogs.stream()
                    .mapToLong(log -> log.getTotalTokens() != null ? log.getTotalTokens() : 0)
                    .sum();

            DailyTokenUsageVO dailyUsage = new DailyTokenUsageVO();
            // 使用中文星期名称
            String dayName = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.CHINA);
            dailyUsage.setDate(dayName);
            dailyUsage.setTokens(dayTokens);

            trend.add(dailyUsage);
        }

        return trend;
    }
}