package com.vela.im.shared.exception;

/**
 * <p>Title: ApplicationException</p>
 * <p>Description: 应用业务异常，继承 RuntimeException，携带错误码和错误消息，由全局异常处理器统一捕获。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @createTime 2025-03-03
 * @updateTime 2026-07-25
 * <p>
 * Copyright © 2026 wanqiu All rights reserved
 * @since 1.0
 */
public class ApplicationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final int code;

    public ApplicationException() {
        super();
        this.code = 0;
    }

    public ApplicationException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 带根因异常的构造器，保留完整异常链以便排查。
     *
     * @param code    错误码
     * @param message 错误消息
     * @param cause   根因异常
     */
    public ApplicationException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public ApplicationException(ApplicationExceptionEnum exceptionEnum) {
        super(exceptionEnum.getError());
        this.code = exceptionEnum.getCode();
    }

    /**
     * 带根因异常的构造器，保留完整异常链以便排查。
     *
     * @param exceptionEnum 错误码枚举
     * @param cause         根因异常
     */
    public ApplicationException(ApplicationExceptionEnum exceptionEnum, Throwable cause) {
        super(exceptionEnum.getError(), cause);
        this.code = exceptionEnum.getCode();
    }

    /**
     * 获取错误消息，委托给 {@link Throwable#getMessage()} 消除字段冗余。
     *
     * @return 错误消息
     */
    public String getError() {
        return super.getMessage();
    }

    /**
     * 获取错误码。
     *
     * @return 错误码
     */
    public int getCode() {
        return code;
    }

    /**
     * API 边界快速失败专用工厂方法 —— 跳过昂贵的堆栈填充以提升性能。
     * <p>适用场景：请求参数校验失败等直接由 {@code GlobalExceptionHandler} 转换为 {@code Result} 返回的异常。
     * <b>普通业务逻辑异常请使用普通构造器</b>，保留完整堆栈以便定位问题代码行。</p>
     *
     * @param exceptionEnum 错误码枚举
     * @return 不带堆栈的 ApplicationException 实例
     */
    public static ApplicationException fast(ApplicationExceptionEnum exceptionEnum) {
        return new ApplicationException(exceptionEnum) {
            @Override
            public synchronized Throwable fillInStackTrace() {
                return this;
            }
        };
    }

}
