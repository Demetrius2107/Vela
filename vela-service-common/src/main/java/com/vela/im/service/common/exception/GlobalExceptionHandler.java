package com.vela.im.service.common.exception;

import com.vela.im.shared.base.Result;
import com.vela.im.shared.exception.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApplicationException.class)
    public Result<Void> handleAppException(ApplicationException e) {
        log.warn("业务异常: code={}, msg={}", e.getCode(), e.getError());
        return Result.fail(e.getCode(), e.getError());
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail(500, "系统内部错误");
    }
}
