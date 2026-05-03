package com.tiv.image.hub.model.dto.space.analysis;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceAnalysisRequest implements Serializable {

    /**
     * 空间id
     */
    private Long spaceId;

    /**
     * 是否分析公共图库
     */
    private boolean analyzePublic;

    /**
     * 是否分析所有空间
     */
    private boolean analyzeAll;

    private static final long serialVersionUID = 1L;

}