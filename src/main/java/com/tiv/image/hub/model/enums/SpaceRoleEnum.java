package com.tiv.image.hub.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 空间角色枚举
 */
@Getter
@AllArgsConstructor
public enum SpaceRoleEnum {

    VIEWER("viewer", "浏览者"),

    EDITOR("editor", "编辑者"),

    ADMIN("admin", "管理员");

    private final String value;

    private final String desc;

    /**
     * 根据 value 获取枚举
     */
    public static SpaceRoleEnum getEnumByValue(String value) {
        if (value == null) {
            return null;
        }
        return Arrays.stream(SpaceRoleEnum.values())
                .filter(spaceRoleEnum -> spaceRoleEnum.getValue().equals(value))
                .findFirst()
                .orElse(null);
    }

}
