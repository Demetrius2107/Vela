package com.vela.im.shared.exception;

import com.vela.im.shared.exception.ApplicationExceptionEnum;

/**
 * <p>Title: BaseErrorCode</p>
 * <p>Description: 基础错误码枚举，实现 ApplicationExceptionEnum 接口，定义通用成功/系统错误码。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @createTime 2025-03-03
 * @updateTime 2026-07-25
 * <p>
 * Copyright © 2026 wanqiu All rights reserved
 * @since 1.0
 */
public enum BaseErrorCode implements ApplicationExceptionEnum {

    /**
     * 成功
     */
    SUCCESS(200, "成功"),
    /**
     * 服务器内部错误，请联系管理员
     */
    SYSTEM_ERROR(90000, "服务器内部错误,请联系管理员"),
    /**
     * 参数校验错误
     */
    PARAMETER_ERROR(90001, "参数校验错误"),
    /**
     * 未登录或身份凭证已过期
     */
    UNAUTHORIZED(90002, "未登录或身份凭证已过期"),
    /**
     * 无操作权限
     */
    FORBIDDEN(90003, "无操作权限"),
    /**
     * 请求过于频繁，请稍后再试
     */
    TOO_MANY_REQUESTS(90004, "请求过于频繁，请稍后再试"),
    /**
     * 服务暂不可用，请稍后重试
     */
    SERVICE_UNAVAILABLE(90005, "服务暂不可用，请稍后重试"),

    ;

    private int code;
    private String error;

    BaseErrorCode(int code, String error) {
        this.code = code;
        this.error = error;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getError() {
        return error;
    }

}
