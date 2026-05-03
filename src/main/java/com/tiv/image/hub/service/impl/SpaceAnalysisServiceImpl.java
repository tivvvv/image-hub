package com.tiv.image.hub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tiv.image.hub.common.BusinessCodeEnum;
import com.tiv.image.hub.exception.BusinessException;
import com.tiv.image.hub.model.dto.space.analysis.SpaceAnalysisRequest;
import com.tiv.image.hub.model.entity.Image;
import com.tiv.image.hub.model.entity.Space;
import com.tiv.image.hub.model.entity.User;
import com.tiv.image.hub.service.SpaceAnalysisService;
import com.tiv.image.hub.service.SpaceService;
import com.tiv.image.hub.service.UserService;
import com.tiv.image.hub.util.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class SpaceAnalysisServiceImpl implements SpaceAnalysisService {

    @Resource
    private SpaceService spaceService;

    @Resource
    private UserService userService;

    private void checkSpaceAnalyzeAuth(SpaceAnalysisRequest spaceAnalysisRequest, User loginUser) {
        // 分析全空间或公共图库-仅管理员可访问
        if (spaceAnalysisRequest.isAnalyzeAll() || spaceAnalysisRequest.isAnalyzePublic()) {
            ThrowUtils.throwIf(!userService.isAdmin(loginUser), BusinessCodeEnum.NO_AUTH_ERROR);
        } else {
            // 分析指定空间-仅本人或管理员可以访问
            Long spaceId = spaceAnalysisRequest.getSpaceId();
            ThrowUtils.throwIf(spaceId == null, BusinessCodeEnum.PARAMS_ERROR, "空间id不能为空");
            Space space = spaceService.getById(spaceId);
            spaceService.checkSpaceAuth(loginUser, space);
        }
    }

    private void fillAnalyzeQueryWrapper(SpaceAnalysisRequest spaceAnalysisRequest, QueryWrapper<Image> queryWrapper) {
        // 分析所有空间
        if (spaceAnalysisRequest.isAnalyzeAll()) {
            return;
        }

        // 分析公共图库
        boolean queryPublic = spaceAnalysisRequest.isAnalyzePublic();
        if (queryPublic) {
            queryWrapper.isNull("spaceId");
            return;
        }

        // 分析指定空间
        Long spaceId = spaceAnalysisRequest.getSpaceId();
        if (spaceId != null) {
            queryWrapper.eq("spaceId", spaceId);
            return;
        }
        throw new BusinessException(BusinessCodeEnum.PARAMS_ERROR, "未指定分析范围");
    }

}