package com.tiv.image.hub.util;

import com.tiv.image.hub.common.BusinessCodeEnum;
import com.tiv.image.hub.common.BusinessResponse;

/**
 * 响应工具类
 */
public class ResultUtils {

    /**
     * 成功
     *
     * @param data
     * @param <T>
     * @return
     */
    public static <T> BusinessResponse<T> success(T data) {
        return new BusinessResponse<>(BusinessCodeEnum.SUCCESS.getCode(), data, BusinessCodeEnum.SUCCESS.getMessage());
    }

    /**
     * 失败
     *
     * @param businessCodeEnum
     * @return
     */
    public static BusinessResponse<?> error(BusinessCodeEnum businessCodeEnum) {
        return new BusinessResponse<>(businessCodeEnum);
    }

    /**
     * 失败
     *
     * @param businessCodeEnum
     * @param data
     * @param <T>
     * @return
     */
    public static <T> BusinessResponse<T> error(BusinessCodeEnum businessCodeEnum, T data) {
        return new BusinessResponse<>(businessCodeEnum, data);
    }

}