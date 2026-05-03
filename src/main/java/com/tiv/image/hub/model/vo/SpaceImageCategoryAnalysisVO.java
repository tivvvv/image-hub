package com.tiv.image.hub.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 空间图片分类分析视图
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpaceImageCategoryAnalysisVO implements Serializable {

    /**
     * 图片分类
     */
    private String imageCategory;

    /**
     * 图片数量
     */
    private Long count;

    /**
     * 分类图片总大小
     */
    private Long totalSize;

    private static final long serialVersionUID = 1L;

}