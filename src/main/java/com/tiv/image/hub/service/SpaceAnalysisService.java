package com.tiv.image.hub.service;

import com.tiv.image.hub.model.dto.space.analysis.SpaceImageCategoryAnalysisRequest;
import com.tiv.image.hub.model.dto.space.analysis.SpaceImageSizeAnalysisRequest;
import com.tiv.image.hub.model.dto.space.analysis.SpaceImageTagAnalysisRequest;
import com.tiv.image.hub.model.dto.space.analysis.SpaceUsageAnalysisRequest;
import com.tiv.image.hub.model.entity.User;
import com.tiv.image.hub.model.vo.SpaceImageCategoryAnalysisVO;
import com.tiv.image.hub.model.vo.SpaceImageSizeAnalysisVO;
import com.tiv.image.hub.model.vo.SpaceImageTagAnalysisVO;
import com.tiv.image.hub.model.vo.SpaceUsageAnalysisVO;

import java.util.List;

public interface SpaceAnalysisService {

    /**
     * 分析空间使用情况
     *
     * @param spaceUsageAnalysisRequest
     * @param loginUser
     * @return
     */
    SpaceUsageAnalysisVO analyzeSpaceUsage(SpaceUsageAnalysisRequest spaceUsageAnalysisRequest, User loginUser);

    /**
     * 分析空间图片分类情况
     *
     * @param spaceImageCategoryAnalysisRequest
     * @param loginUser
     * @return
     */
    List<SpaceImageCategoryAnalysisVO> analyzeSpaceImageCategory(SpaceImageCategoryAnalysisRequest spaceImageCategoryAnalysisRequest, User loginUser);

    /**
     * 分析空间图片标签情况
     *
     * @param spaceImageTagAnalysisRequest
     * @param loginUser
     * @return
     */
    List<SpaceImageTagAnalysisVO> analyzeSpaceImageTag(SpaceImageTagAnalysisRequest spaceImageTagAnalysisRequest, User loginUser);

    /**
     * 分析空间图片大小情况
     *
     * @param spaceImageSizeAnalysisRequest
     * @param loginUser
     * @return
     */
    List<SpaceImageSizeAnalysisVO> analyzeSpaceImageSize(SpaceImageSizeAnalysisRequest spaceImageSizeAnalysisRequest, User loginUser);

}