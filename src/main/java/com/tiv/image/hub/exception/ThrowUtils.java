package com.tiv.image.hub.exception;

/**
 * 异常处理工具类
 */
public class ThrowUtils {

    /**
     * 条件成立则抛异常
     *
     * @param condition
     * @param runtimeException
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if (condition) {
            throw runtimeException;
        }
    }

    /**
     * 条件成立则抛异常
     *
     * @param condition
     * @param businessCode
     */
    public static void throwIf(boolean condition, BusinessCode businessCode) {
        throwIf(condition, new BusinessException(businessCode));
    }

    /**
     * 条件成立则抛异常
     *
     * @param condition
     * @param businessCode
     * @param message
     */
    public static void throwIf(boolean condition, BusinessCode businessCode, String message) {
        throwIf(condition, new BusinessException(businessCode, message));
    }

}
