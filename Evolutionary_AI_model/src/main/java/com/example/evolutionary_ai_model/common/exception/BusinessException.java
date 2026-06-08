package com.example.evolutionary_ai_model.common.exception;

/**
 * 用法：业务异常类，用于封装业务逻辑中可预见的异常情况。
 * 在 Service 层抛出此异常，由全局异常处理器统一处理并返回友好的错误信息。
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    //错误码
    private int code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}