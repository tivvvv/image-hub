package com.tiv.image.hub.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.tiv.image.hub.common.BusinessCodeEnum;
import com.tiv.image.hub.common.BusinessResponse;
import com.tiv.image.hub.util.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常处理
     *
     * @param e
     * @return
     */
    @ExceptionHandler(BusinessException.class)
    public BusinessResponse<?> handleBusinessException(BusinessException e) {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    /**
     * sa-token未登录异常处理
     *
     * @param e
     * @return
     */
    @ExceptionHandler(NotLoginException.class)
    public BusinessResponse<?> handleNotLoginException(NotLoginException e) {
        log.error("NotLoginException", e);
        return ResultUtils.error(BusinessCodeEnum.NOT_LOGIN_ERROR);
    }

    /**
     * sa-token无角色异常处理
     *
     * @param e
     * @return
     */
    @ExceptionHandler(NotRoleException.class)
    public BusinessResponse<?> handleNotRoleException(NotRoleException e) {
        log.error("NotRoleException", e);
        return ResultUtils.error(BusinessCodeEnum.NO_AUTH_ERROR);
    }

    /**
     * sa-token无权限异常处理
     *
     * @param e
     * @return
     */
    @ExceptionHandler(NotPermissionException.class)
    public BusinessResponse<?> handleNotPermissionException(NotPermissionException e) {
        log.error("NotPermissionException", e);
        return ResultUtils.error(BusinessCodeEnum.NO_AUTH_ERROR);
    }

    /**
     * 参数校验异常处理
     *
     * @param e
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BusinessResponse<String> handleValidationException(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.joining());
        return ResultUtils.error(BusinessCodeEnum.PARAMS_ERROR, errorMsg);
    }

    /**
     * 运行时异常处理
     *
     * @param e
     * @return
     */
    @ExceptionHandler(RuntimeException.class)
    public BusinessResponse<?> handleRuntimeException(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(BusinessCodeEnum.SYSTEM_ERROR);
    }

    /**
     * 异常处理
     *
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    public BusinessResponse<?> handleException(Exception e) {
        log.error("Exception", e);
        return ResultUtils.error(BusinessCodeEnum.SYSTEM_ERROR);
    }

}