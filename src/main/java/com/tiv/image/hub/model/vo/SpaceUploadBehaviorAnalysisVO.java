package com.tiv.image.hub.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 空间上传行为分析视图
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpaceUploadBehaviorAnalysisVO implements Serializable {

    /**
     * 时间范围
     */
    private String timeRange;

    /**
     * 图片数量
     */
    private Long count;

    private static final long serialVersionUID = 1L;

}