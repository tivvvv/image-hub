package com.tiv.image.hub.model.dto.space.analysis;

import com.tiv.image.hub.model.enums.TimeDimensionEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 空间上传行为分析请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SpaceUploadBehaviorAnalysisRequest extends SpaceAnalysisRequest {

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 时间维度
     */
    private TimeDimensionEnum timeDimension;

}