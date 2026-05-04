package com.tiv.image.hub.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 空间图片大小分析视图
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpaceImageSizeAnalysisVO implements Serializable {

    /**
     * 图片大小范围
     */
    private String sizeRange;

    /**
     * 图片数量
     */
    private Long count;

    private static final long serialVersionUID = 1L;

}