package com.tiv.image.hub.service;

import com.tiv.image.hub.model.dto.space.analysis.SpaceUsageAnalysisRequest;
import com.tiv.image.hub.model.entity.User;
import com.tiv.image.hub.model.vo.SpaceUsageAnalysisVO;

public interface SpaceAnalysisService {

    /**
     * 分析空间使用情况
     *
     * @param spaceUsageAnalysisRequest
     * @param loginUser
     * @return
     */
    SpaceUsageAnalysisVO analyzeSpaceUsage(SpaceUsageAnalysisRequest spaceUsageAnalysisRequest, User loginUser);

}