package com.tiv.image.hub.model.enums;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;

/**
 * 用户角色枚举
 */
@AllArgsConstructor
public enum UserRoleEnum {

    USER("user", "普通用户"),
    VIP("vip", "VIP用户"),
    BANNED("banned", "封禁用户"),
    ADMIN("admin", "管理员");

    public final String value;

    public final String desc;

    public static UserRoleEnum getEnumByValue(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        for (UserRoleEnum userRoleEnum : UserRoleEnum.values()) {
            if (userRoleEnum.value.equals(value)) {
                return userRoleEnum;
            }
        }
        return null;
    }

}
