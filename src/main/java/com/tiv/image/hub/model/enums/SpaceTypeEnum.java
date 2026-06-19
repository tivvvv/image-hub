package com.tiv.image.hub.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 空间类型枚举
 */
@Getter
@AllArgsConstructor
public enum SpaceTypeEnum {

    PRIVATE(0, "私有空间"),

    TEAM(1, "团队空间");

    public final int value;

    public final String desc;

    /**
     * 根据 value 获取枚举
     */
    public static SpaceTypeEnum getEnumByValue(Integer value) {
        if (value == null) {
            return null;
        }
        return Arrays.stream(SpaceTypeEnum.values())
                .filter(spaceLevelEnum -> spaceLevelEnum.value == value)
                .findFirst()
                .orElse(null);
    }

}