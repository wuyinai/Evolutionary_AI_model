package com.example.evolutionary_ai_model.entity.enums;

/**
 * 用法：AI模型协议枚举，定义支持的AI服务协议类型。
 * 用于标识不同供应商的API协议，驱动工厂创建对应的ChatModel实例。
 * 支持OpenAI兼容协议、Anthropic协议、Ollama协议等主流协议。
 */
public enum ModelProtocol {

    // OpenAI兼容协议（包括OpenAI、Azure OpenAI、DeepSeek、通义千问等）
    OPENAI("OPENAI", "OpenAI兼容协议", "使用OpenAI API格式，支持chat/completions接口"),

    // Anthropic Claude协议
    ANTHROPIC("ANTHROPIC", "Anthropic协议", "使用Anthropic Claude API格式"),

    // Ollama本地部署协议
    OLLAMA("OLLAMA", "Ollama协议", "使用Ollama本地部署API格式"),

    // 百度文心一言协议（需要特殊认证方式）
    ERNIE("ERNIE", "文心一言协议", "使用百度文心一言API格式，需要access token"),

    // 智谱清言协议
    ZHIPU("ZHIPU", "智谱清言协议", "使用智谱GLM API格式"),

    // Moonshot Kimi协议
    MOONSHOT("MOONSHOT", "Moonshot协议", "使用Moonshot API格式");

    // 协议编码
    private final String code;

    // 协议名称
    private final String name;

    // 协议描述
    private final String description;

    ModelProtocol(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据编码获取协议枚举
     * @param code 协议编码
     * @return 协议枚举，如果不存在则返回null
     */
    public static ModelProtocol fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        for (ModelProtocol protocol : values()) {
            if (protocol.getCode().equalsIgnoreCase(code)) {
                return protocol;
            }
        }
        return null;
    }

    /**
     * 根据供应商编码推断协议类型
     * @param providerCode 供应商编码
     * @return 协议枚举
     */
    public static ModelProtocol fromProviderCode(String providerCode) {
        if (providerCode == null || providerCode.isEmpty()) {
            return OPENAI; // 默认使用OpenAI协议
        }

        // 根据供应商编码映射协议
        switch (providerCode.toUpperCase()) {
            case "OPENAI":
            case "DEEPSEEK":
            case "QWEN":
            case "AZURE_OPENAI":
                return OPENAI;
            case "CLAUDE":
            case "ANTHROPIC":
                return ANTHROPIC;
            case "OLLAMA":
                return OLLAMA;
            case "ERNIE":
                return ERNIE;
            case "ZHIPU":
                return ZHIPU;
            case "MOONSHOT":
                return MOONSHOT;
            default:
                return OPENAI; // 默认使用OpenAI兼容协议
        }
    }
}