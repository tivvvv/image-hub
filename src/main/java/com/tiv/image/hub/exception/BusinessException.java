package com.tiv.image.hub.exception;

import lombok.Getter;

/**
 * 自定义业务异常
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(BusinessCode businessCode) {
        super(businessCode.getMessage());
        this.code = businessCode.getCode();
    }

    public BusinessException(BusinessCode businessCode, String message) {
        super(message);
        this.code = businessCode.getCode();
    }

}
