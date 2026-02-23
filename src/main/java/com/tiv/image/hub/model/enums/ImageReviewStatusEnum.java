package com.tiv.image.hub.model.enums;

import lombok.AllArgsConstructor;

/**
 * 图片审核状态枚举
 */
@AllArgsConstructor
public enum ImageReviewStatusEnum {

    REVIEWING(0, "审核中"),
    PASS(1, "通过"),
    REJECT(2, "拒绝");

    public final int value;

    public final String desc;

    public static ImageReviewStatusEnum getEnumByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (ImageReviewStatusEnum imageReviewStatusEnum : ImageReviewStatusEnum.values()) {
            if (imageReviewStatusEnum.value == value) {
                return imageReviewStatusEnum;
            }
        }
        return null;
    }

}
