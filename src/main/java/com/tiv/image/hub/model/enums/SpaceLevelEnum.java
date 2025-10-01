package com.tiv.image.hub.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 空间级别枚举
 */
@Getter
@AllArgsConstructor
public enum SpaceLevelEnum {

    COMMON(0, "普通版", 1_000L * 1024 * 1024, 1_000),
    PRO(1, "专业版", 10_000L * 1024 * 1024, 10_000),
    ULTRA(2, "旗舰版", 100_000L * 1024 * 1024, 100_000);

    public final int value;

    public final String desc;

    private final long baseMaxSize;

    private final long baseMaxCount;

    /**
     * 根据 value 获取枚举
     */
    public static SpaceLevelEnum getEnumByValue(Integer value) {
        if (value == null) {
            return null;
        }
        return Arrays.stream(SpaceLevelEnum.values())
                .filter(spaceLevelEnum -> spaceLevelEnum.value == value)
                .findFirst()
                .orElse(null);
    }

}