package com.tiv.image.hub.controller;

import com.tiv.image.hub.common.BusinessResponse;
import com.tiv.image.hub.model.dto.space.analysis.*;
import com.tiv.image.hub.model.entity.User;
import com.tiv.image.hub.model.vo.*;
import com.tiv.image.hub.service.SpaceAnalysisService;
import com.tiv.image.hub.service.UserService;
import com.tiv.image.hub.util.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/space/analysis")
public class SpaceAnalysisController {

    @Resource
    private UserService userService;

    @Resource
    private SpaceAnalysisService spaceAnalysisService;

    /**
     * 分析空间使用情况
     *
     * @param spaceUsageAnalysisRequest
     * @return
     */
    @PostMapping("/usage")
    public BusinessResponse<SpaceUsageAnalysisVO> analyzeSpaceUsage(@RequestBody SpaceUsageAnalysisRequest spaceUsageAnalysisRequest) {
        User loginUser = userService.getLoginUser();
        return ResultUtils.success(spaceAnalysisService.analyzeSpaceUsage(spaceUsageAnalysisRequest, loginUser));
    }

    /**
     * 分析空间图片分类情况
     *
     * @param spaceImageCategoryAnalysisRequest
     * @return
     */
    @PostMapping("/image/category")
    public BusinessResponse<List<SpaceImageCategoryAnalysisVO>> analyzeSpaceImageCategory(@RequestBody SpaceImageCategoryAnalysisRequest spaceImageCategoryAnalysisRequest) {
        User loginUser = userService.getLoginUser();
        return ResultUtils.success(spaceAnalysisService.analyzeSpaceImageCategory(spaceImageCategoryAnalysisRequest, loginUser));
    }

    /**
     * 分析空间图片标签情况
     *
     * @param spaceImageTagAnalysisRequest
     * @return
     */
    @PostMapping("/image/tag")
    public BusinessResponse<List<SpaceImageTagAnalysisVO>> analyzeSpaceImageTag(@RequestBody SpaceImageTagAnalysisRequest spaceImageTagAnalysisRequest) {
        User loginUser = userService.getLoginUser();
        return ResultUtils.success(spaceAnalysisService.analyzeSpaceImageTag(spaceImageTagAnalysisRequest, loginUser));
    }

    /**
     * 分析空间图片大小情况
     *
     * @param spaceImageSizeAnalysisRequest
     * @return
     */
    @PostMapping("/image/size")
    public BusinessResponse<List<SpaceImageSizeAnalysisVO>> analyzeSpaceImageSize(@RequestBody SpaceImageSizeAnalysisRequest spaceImageSizeAnalysisRequest) {
        User loginUser = userService.getLoginUser();
        return ResultUtils.success(spaceAnalysisService.analyzeSpaceImageSize(spaceImageSizeAnalysisRequest, loginUser));
    }

    /**
     * 分析空间上传行为情况
     *
     * @param spaceUploadBehaviorAnalysisRequest
     * @return
     */
    @PostMapping("/upload/behavior")
    public BusinessResponse<List<SpaceUploadBehaviorAnalysisVO>> analyzeSpaceUploadBehavior(@RequestBody SpaceUploadBehaviorAnalysisRequest spaceUploadBehaviorAnalysisRequest) {
        User loginUser = userService.getLoginUser();
        return ResultUtils.success(spaceAnalysisService.analyzeSpaceUploadBehavior(spaceUploadBehaviorAnalysisRequest, loginUser));
    }

}