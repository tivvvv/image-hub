package com.tiv.image.hub.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tiv.image.hub.common.BusinessCodeEnum;
import com.tiv.image.hub.exception.BusinessException;
import com.tiv.image.hub.model.dto.space.analysis.SpaceAnalysisRequest;
import com.tiv.image.hub.model.dto.space.analysis.SpaceImageCategoryAnalysisRequest;
import com.tiv.image.hub.model.dto.space.analysis.SpaceImageTagAnalysisRequest;
import com.tiv.image.hub.model.dto.space.analysis.SpaceUsageAnalysisRequest;
import com.tiv.image.hub.model.entity.Image;
import com.tiv.image.hub.model.entity.Space;
import com.tiv.image.hub.model.entity.User;
import com.tiv.image.hub.model.vo.SpaceImageCategoryAnalysisVO;
import com.tiv.image.hub.model.vo.SpaceImageTagAnalysisVO;
import com.tiv.image.hub.model.vo.SpaceUsageAnalysisVO;
import com.tiv.image.hub.service.ImageService;
import com.tiv.image.hub.service.SpaceAnalysisService;
import com.tiv.image.hub.service.SpaceService;
import com.tiv.image.hub.service.UserService;
import com.tiv.image.hub.util.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SpaceAnalysisServiceImpl implements SpaceAnalysisService {

    @Resource
    private SpaceService spaceService;

    @Resource
    private UserService userService;

    @Resource
    private ImageService imageService;

    @Override
    public SpaceUsageAnalysisVO analyzeSpaceUsage(SpaceUsageAnalysisRequest spaceUsageAnalysisRequest, User loginUser) {
        if (spaceUsageAnalysisRequest.isAnalyzeAll() || spaceUsageAnalysisRequest.isAnalyzePublic()) {
            return analyzeAllSpacesOrPublicSpaceUsage(spaceUsageAnalysisRequest, loginUser);
        }
        return analyzePrivateSpaceUsage(spaceUsageAnalysisRequest, loginUser);
    }

    private SpaceUsageAnalysisVO analyzeAllSpacesOrPublicSpaceUsage(SpaceUsageAnalysisRequest spaceUsageAnalysisRequest, User loginUser) {
        // 校验权限,仅管理员可以分析
        checkSpaceAnalyzeAuth(spaceUsageAnalysisRequest, loginUser);

        QueryWrapper<Image> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("count(1) as count", "ifnull(sum(image_size), 0) as totalSize");
        fillAnalyzeQueryWrapper(spaceUsageAnalysisRequest, queryWrapper);

        Map<String, Object> resultMap = imageService.getBaseMapper().selectMaps(queryWrapper).get(0);
        long usedSize = ((Number) resultMap.get("totalSize")).longValue();
        long usedCount = ((Number) resultMap.get("count")).longValue();
        return SpaceUsageAnalysisVO.builder()
                .currentSize(usedSize)
                .currentCount(usedCount)
                .build();
    }

    private SpaceUsageAnalysisVO analyzePrivateSpaceUsage(SpaceUsageAnalysisRequest spaceUsageAnalysisRequest, User loginUser) {
        // 校验权限
        checkSpaceAnalyzeAuth(spaceUsageAnalysisRequest, loginUser);
        Space space = spaceService.getById(spaceUsageAnalysisRequest.getSpaceId());

        return SpaceUsageAnalysisVO.builder()
                .currentSize(space.getCurrentSize())
                .maxSize(space.getMaxSize())
                .currentCount(space.getCurrentCount())
                .maxCount(space.getMaxCount())
                .sizeUsageRatio(NumberUtil.round(space.getCurrentSize() * 100.0 / space.getMaxSize(), 2).doubleValue())
                .countUsageRatio(NumberUtil.round(space.getCurrentCount() * 100.0 / space.getMaxCount(), 2).doubleValue())
                .build();
    }

    @Override
    public List<SpaceImageCategoryAnalysisVO> analyzeSpaceImageCategory(SpaceImageCategoryAnalysisRequest spaceImageCategoryAnalysisRequest, User loginUser) {
        checkSpaceAnalyzeAuth(spaceImageCategoryAnalysisRequest, loginUser);
        QueryWrapper<Image> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("image_category as imageCategory", "count(*) as count", "sum(image_size) as totalSize")
                .groupBy("image_category");
        fillAnalyzeQueryWrapper(spaceImageCategoryAnalysisRequest, queryWrapper);
        return imageService.getBaseMapper().selectMaps(queryWrapper)
                .stream()
                .map(map -> {
                    return SpaceImageCategoryAnalysisVO.builder()
                            .imageCategory((String) map.get("imageCategory"))
                            .count((Long) map.get("count"))
                            .totalSize((Long) map.get("totalSize"))
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<SpaceImageTagAnalysisVO> analyzeSpaceImageTag(SpaceImageTagAnalysisRequest spaceImageTagAnalysisRequest, User loginUser) {
        checkSpaceAnalyzeAuth(spaceImageTagAnalysisRequest, loginUser);

        QueryWrapper<Image> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("image_tags");
        fillAnalyzeQueryWrapper(spaceImageTagAnalysisRequest, queryWrapper);

        // 查询所有标签
        List<String> tagsJsonList = imageService.getBaseMapper().selectObjs(queryWrapper)
                .stream()
                .filter(ObjUtil::isNotNull)
                .map(Object::toString)
                .collect(Collectors.toList());

        // 解析标签并统计
        Map<String, Long> tagCountMap = tagsJsonList.stream()
                .flatMap(tagsJson -> JSONUtil.toList(tagsJson, String.class).stream())
                .collect(Collectors.groupingBy(tag -> tag, Collectors.counting()));

        return tagCountMap.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .map(entry -> {
                    return SpaceImageTagAnalysisVO.builder()
                            .tag(entry.getKey())
                            .count(entry.getValue())
                            .build();
                })
                .collect(Collectors.toList());
    }


    private void checkSpaceAnalyzeAuth(SpaceAnalysisRequest spaceAnalysisRequest, User loginUser) {
        // 分析所有空间或公共图库-仅管理员可访问
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