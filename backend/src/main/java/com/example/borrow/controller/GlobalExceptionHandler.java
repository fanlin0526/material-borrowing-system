package com.example.borrow.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.persistence.EntityNotFoundException;
import javax.persistence.OptimisticLockException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;

/**
 * 全局异常处理器 — 统一返回 JSON 格式错误，避免 Spring Boot 默认 HTML 错误页
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常 */
    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntime(RuntimeException e) {
        return Result.fail(e.getMessage());
    }

    /** 实体不存在 */
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<?> handleNotFound(EntityNotFoundException e) {
        return Result.fail("资源不存在: " + e.getMessage());
    }

    /** 并发修改库存或单据时给出可理解的提示，而不是暴露数据库异常 */
    @ExceptionHandler({OptimisticLockingFailureException.class, OptimisticLockException.class,
            PessimisticLockingFailureException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public Result<?> handleConcurrentConflict(Exception e) {
        return Result.fail("该物资刚被其他操作修改，库存或状态已发生变化，请刷新后重试");
    }

    /** 请求参数校验失败（@Valid） */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        return Result.fail(msg);
    }

    /** 缺少必填参数 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMissingParam(MissingServletRequestParameterException e) {
        return Result.fail("缺少参数: " + e.getParameterName());
    }

    /** JSON 解析失败 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBadJson(HttpMessageNotReadableException e) {
        return Result.fail("请求数据格式错误");
    }

    /** 兜底：所有未捕获的异常 */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleAll(Exception e) {
        return Result.fail("服务器内部错误: " + e.getMessage());
    }
}
