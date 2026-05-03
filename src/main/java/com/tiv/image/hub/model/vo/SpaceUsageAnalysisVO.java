package com.tiv.image.hub.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 空间使用情况分析视图
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpaceUsageAnalysisVO implements Serializable {

    /**
     * 空间id
     */
    private Long id;

    /**
     * 当前空间已使用容量
     */
    private Long currentSize;

    /**
     * 空间图片的最大容量
     */
    private Long maxSize;

    /**
     * 空间容量使用比例
     */
    private Double sizeUsageRatio;

    /**
     * 当前空间已使用数量
     */
    private Long currentCount;

    /**
     * 空间图片的最大数量
     */
    private Long maxCount;

    /**
     * 图片数量使用比例
     */
    private Double countUsageRatio;

    private static final long serialVersionUID = 1L;

}