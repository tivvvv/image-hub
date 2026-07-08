package com.tiv.image.hub.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tiv.image.hub.common.BusinessCodeEnum;
import com.tiv.image.hub.constant.Constants;
import com.tiv.image.hub.exception.BusinessException;
import com.tiv.image.hub.model.dto.space.analysis.*;
import com.tiv.image.hub.model.entity.Image;
import com.tiv.image.hub.model.entity.Space;
import com.tiv.image.hub.model.entity.User;
import com.tiv.image.hub.model.vo.*;
import com.tiv.image.hub.service.ImageService;
import com.tiv.image.hub.service.SpaceAnalysisService;
import com.tiv.image.hub.service.SpaceService;
import com.tiv.image.hub.service.UserService;
import com.tiv.image.hub.util.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
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

    @Override
    public List<SpaceImageSizeAnalysisVO> analyzeSpaceImageSize(SpaceImageSizeAnalysisRequest spaceImageSizeAnalysisRequest, User loginUser) {
        checkSpaceAnalyzeAuth(spaceImageSizeAnalysisRequest, loginUser);

        QueryWrapper<Image> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("image_size");
        fillAnalyzeQueryWrapper(spaceImageSizeAnalysisRequest, queryWrapper);

        List<Long> picSizeList = imageService.getBaseMapper().selectObjs(queryWrapper)
                .stream()
                .filter(ObjUtil::isNotNull)
                .map(size -> (Long) size)
                .collect(Collectors.toList());

        // 定义分段范围, 使用有序的 Map
        Map<String, Long> sizeRanges = new LinkedHashMap<>();
        sizeRanges.put("<100KB", picSizeList.stream().filter(size -> size < 100 * 1024).count());
        sizeRanges.put("100KB-500KB", picSizeList.stream().filter(size -> size >= 100 * 1024 && size < 500 * 1024).count());
        sizeRanges.put("500KB-1MB", picSizeList.stream().filter(size -> size >= 500 * 1024 && size < 1024 * 1024).count());
        sizeRanges.put(">1MB", picSizeList.stream().filter(size -> size >= 1024 * 1024).count());

        return sizeRanges.entrySet().stream()
                .map(entry -> SpaceImageSizeAnalysisVO.builder().sizeRange(entry.getKey()).count(entry.getValue()).build())
                .collect(Collectors.toList());
    }

    @Override
    public List<SpaceUploadBehaviorAnalysisVO> analyzeSpaceUploadBehavior(SpaceUploadBehaviorAnalysisRequest spaceUploadBehaviorAnalysisRequest, User loginUser) {
        // 校验权限
        checkSpaceAnalyzeAuth(spaceUploadBehaviorAnalysisRequest, loginUser);

        // 构造查询条件
        QueryWrapper<Image> queryWrapper = new QueryWrapper<>();
        Long userId = spaceUploadBehaviorAnalysisRequest.getUserId();
        queryWrapper.eq(userId != null, "user_id", userId);
        fillAnalyzeQueryWrapper(spaceUploadBehaviorAnalysisRequest, queryWrapper);

        switch (spaceUploadBehaviorAnalysisRequest.getTimeDimension()) {
            case DAY:
                queryWrapper.select("DATE_FORMAT(create_time, '%Y-%m-%d') as timeRange", "count(*) as count");
                break;
            case WEEK:
                queryWrapper.select("DATE_FORMAT(create_time, '%Y-%u') as timeRange", "count(*) as count");
                break;
            case MONTH:
                queryWrapper.select("DATE_FORMAT(create_time, '%Y-%m') as timeRange", "count(*) as count");
                break;
            case YEAR:
                queryWrapper.select("DATE_FORMAT(create_time, '%Y') as timeRange", "count(*) as count");
                break;
            default:
                throw new BusinessException(BusinessCodeEnum.PARAMS_ERROR, "不支持的时间维度");
        }

        // 分组排序
        queryWrapper.groupBy("timeRange")
                .orderByAsc("timeRange");

        return imageService.getBaseMapper().selectMaps(queryWrapper)
                .stream()
                .map(map -> SpaceUploadBehaviorAnalysisVO.builder()
                        .timeRange((String) map.get("timeRange"))
                        .count((Long) map.get("count"))
                        .build())
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
            queryWrapper.and(wrapper -> wrapper.isNull("space_id").or().eq("space_id", Constants.PUBLIC_SPACE_ID));
            return;
        }

        // 分析指定空间
        Long spaceId = spaceAnalysisRequest.getSpaceId();
        if (spaceId != null) {
            queryWrapper.eq("space_id", spaceId);
            return;
        }
        throw new BusinessException(BusinessCodeEnum.PARAMS_ERROR, "未指定分析范围");
    }

}
