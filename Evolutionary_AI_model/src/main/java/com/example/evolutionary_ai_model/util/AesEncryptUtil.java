package com.example.evolutionary_ai_model.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * 用法：AES加密工具类，用于API密钥的加密存储和解密读取。
 * 使用Hutool工具库提供的AES加密功能，密钥从配置文件读取。
 */
public class AesEncryptUtil {

    private static final Logger logger = LoggerFactory.getLogger(AesEncryptUtil.class);

    // AES加密密钥（16位），生产环境建议从配置文件或环境变量读取
    private static final String AES_KEY = "EvolutionaryAI16";

    private static final AES aes = SecureUtil.aes(AES_KEY.getBytes(StandardCharsets.UTF_8));

    /**
     * 加密字符串
     * @param plainText 明文
     * @return 加密后的字符串（Base64编码）
     */
    public static String encrypt(String plainText) {
        if (StrUtil.isBlank(plainText)) {
            return plainText;
        }
        try {
            return aes.encryptBase64(plainText);
        } catch (Exception e) {
            logger.error("AES加密失败", e);
            throw new RuntimeException("加密失败");
        }
    }

    /**
     * 解密字符串
     * @param encryptedText 加密后的字符串（Base64编码）
     * @return 解密后的明文
     */
    public static String decrypt(String encryptedText) {
        if (StrUtil.isBlank(encryptedText)) {
            return encryptedText;
        }
        try {
            return aes.decryptStr(encryptedText);
        } catch (Exception e) {
            logger.error("AES解密失败", e);
            throw new RuntimeException("解密失败");
        }
    }

    /**
     * 对API密钥进行脱敏处理
     * @param apiKey API密钥
     * @return 脱敏后的密钥，如：sk-****xxx
     */
    public static String maskApiKey(String apiKey) {
        if (StrUtil.isBlank(apiKey) || apiKey.length() < 8) {
            return "****";
        }
        // 显示前4位和后3位，中间用****替代
        int prefixLength = Math.min(4, apiKey.length() - 3);
        int suffixLength = 3;
        return apiKey.substring(0, prefixLength) + "****" + apiKey.substring(apiKey.length() - suffixLength);
    }
}