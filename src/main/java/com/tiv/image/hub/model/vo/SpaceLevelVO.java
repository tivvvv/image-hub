package com.tiv.image.hub.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 空间级别视图
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpaceLevelVO {

    private int value;

    private String desc;

    private long baseMaxSize;

    private long baseMaxCount;

}
