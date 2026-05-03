package com.tiv.image.hub.controller;

import com.tiv.image.hub.common.BusinessResponse;
import com.tiv.image.hub.model.dto.space.analysis.SpaceUsageAnalysisRequest;
import com.tiv.image.hub.model.entity.User;
import com.tiv.image.hub.model.vo.SpaceUsageAnalysisVO;
import com.tiv.image.hub.service.SpaceAnalysisService;
import com.tiv.image.hub.service.UserService;
import com.tiv.image.hub.util.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/usage")
    public BusinessResponse<SpaceUsageAnalysisVO> analyzeSpaceUsage(@RequestBody SpaceUsageAnalysisRequest spaceUsageAnalysisRequest,
                                                                    HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(spaceAnalysisService.analyzeSpaceUsage(spaceUsageAnalysisRequest, loginUser));
    }

}