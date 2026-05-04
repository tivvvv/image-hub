package com.tiv.image.hub.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 空间图片标签分析视图
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpaceImageTagAnalysisVO implements Serializable {

    /**
     * 标签
     */
    private String tag;

    /**
     * 图片数量
     */
    private Long count;

    private static final long serialVersionUID = 1L;

}