package com.tiv.image.hub.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.tiv.image.hub.annotation.AuthCheck;
import com.tiv.image.hub.common.BusinessCodeEnum;
import com.tiv.image.hub.common.BusinessResponse;
import com.tiv.image.hub.common.DeleteRequest;
import com.tiv.image.hub.constant.Constants;
import com.tiv.image.hub.model.dto.image.request.*;
import com.tiv.image.hub.model.entity.Image;
import com.tiv.image.hub.model.entity.Space;
import com.tiv.image.hub.model.entity.User;
import com.tiv.image.hub.model.enums.ImageReviewStatusEnum;
import com.tiv.image.hub.model.vo.ImageTagCategory;
import com.tiv.image.hub.model.vo.ImageVO;
import com.tiv.image.hub.service.ImageService;
import com.tiv.image.hub.service.SpaceService;
import com.tiv.image.hub.service.UserService;
import com.tiv.image.hub.util.ColorSimilarUtils;
import com.tiv.image.hub.util.ResultUtils;
import com.tiv.image.hub.util.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.awt.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 图片controller
 */
@Slf4j
@RestController
@RequestMapping("/image")
public class ImageController {

    @Resource
    private ImageService imageService;

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 本地缓存
     */
    private final Cache<String, String> localCache = Caffeine.newBuilder()
            .initialCapacity(1024)
            .maximumSize(10_000L)
            .expireAfterWrite(Duration.ofMinutes(5))
            .build();

    private static final int USER_QUERY_PICTURE_LIMIT = 30;

    /**
     * 上传图片
     *
     * @param multipartFile
     * @param imageUploadRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/upload")
    public BusinessResponse<ImageVO> uploadImage(@RequestPart("file") MultipartFile multipartFile,
                                                 ImageUploadRequest imageUploadRequest,
                                                 HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(imageService.uploadImage(multipartFile, imageUploadRequest, loginUser));
    }

    /**
     * 根据url上传图片
     *
     * @param imageUploadRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/upload/url")
    public BusinessResponse<ImageVO> uploadImageByUrl(@RequestBody ImageUploadRequest imageUploadRequest,
                                                      HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        String fileUrl = imageUploadRequest.getFileUrl();
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), BusinessCodeEnum.PARAMS_ERROR, "文件url不能为空");
        return ResultUtils.success(imageService.uploadImage(fileUrl, imageUploadRequest, loginUser));
    }

    /**
     * 更新图片
     *
     * @param imageUpdateRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/update")
    public BusinessResponse<Boolean> updateImage(@RequestBody @Valid ImageUpdateRequest imageUpdateRequest,
                                                 HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(imageService.updateImage(imageUpdateRequest, loginUser));
    }

    /**
     * 根据id获取图片视图
     */
    @GetMapping("/vo/{id}")
    public BusinessResponse<ImageVO> getImageVOById(@PathVariable long id, HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        Image image = doGetImage(id, loginUser);
        // 用户只能查询审核通过的图片
        ThrowUtils.throwIf(image.getReviewStatus() != ImageReviewStatusEnum.PASS.value,
                BusinessCodeEnum.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(imageService.getImageVO(image));
    }

    /**
     * 分页获取图片视图列表
     */
    @PostMapping("/page/vo")
    public BusinessResponse<Page<ImageVO>> listImageVOByPage(@RequestBody ImageQueryRequest imageQueryRequest, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(imageQueryRequest.getPageSize() > USER_QUERY_PICTURE_LIMIT,
                BusinessCodeEnum.PARAMS_ERROR, "查询数量过多");
        // 用户只能查询审核通过的图片
        imageQueryRequest.setReviewStatus(ImageReviewStatusEnum.PASS.value);
        Page<Image> imagePage = doListImage(imageQueryRequest, userService.getLoginUser(httpServletRequest));
        // 获取封装类
        return ResultUtils.success(imageService.getImageVOPage(imagePage));
    }

    /**
     * 分页获取图片视图列表(带缓存)
     */
    @PostMapping("/page/vo/cache")
    public BusinessResponse<Page<ImageVO>> listImageVOByPageWithCache(@RequestBody ImageQueryRequest imageQueryRequest, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(imageQueryRequest.getPageSize() > USER_QUERY_PICTURE_LIMIT,
                BusinessCodeEnum.PARAMS_ERROR, "查询数量过多");
        // 用户只能查询审核通过的图片
        imageQueryRequest.setReviewStatus(ImageReviewStatusEnum.PASS.value);

        // 1. 查询本地缓存
        String queryCondition = JSONUtil.toJsonStr(imageQueryRequest);
        String hashKey = DigestUtil.md5Hex(queryCondition.getBytes());
        // 缓存key格式: 项目名:方法名:md5
        String cacheKey = String.format("%s:%s:%s", Constants.PROJECT, "listImageVOByPageWithCache", hashKey);
        String cachedValue = localCache.getIfPresent(cacheKey);
        if (StrUtil.isNotBlank(cachedValue)) {
            // 1.1 本地缓存命中,直接返回结果
            Page<ImageVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
            return ResultUtils.success(cachedPage);
        }
        // 2. 本地缓存未命中,查询redis缓存
        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
        cachedValue = opsForValue.get(cacheKey);
        if (StrUtil.isNotBlank(cachedValue)) {
            // 2.1 redis缓存命中,更新本地缓存
            localCache.put(cacheKey, cachedValue);
            Page<ImageVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
            return ResultUtils.success(cachedPage);
        }

        // 3. 多级缓存都未命中,查询数据库
        Page<Image> imagePage = doListImage(imageQueryRequest, userService.getLoginUser(httpServletRequest));
        // 获取封装类
        Page<ImageVO> imageVOPage = imageService.getImageVOPage(imagePage);

        // 4. 更新本地缓存
        localCache.put(cacheKey, JSONUtil.toJsonStr(imageVOPage));

        // 5. 更新redis缓存
        // 缓存时间5-10min,避免缓存雪崩
        int expireTime = 300 + RandomUtil.randomInt(0, 300);
        opsForValue.set(cacheKey, JSONUtil.toJsonStr(imageVOPage), expireTime, TimeUnit.SECONDS);

        return ResultUtils.success(imageVOPage);
    }

    /**
     * 删除图片
     *
     * @param deleteRequest
     * @param httpServletRequest
     * @return
     */
    @DeleteMapping()
    public BusinessResponse<Boolean> deleteImage(@RequestBody @Valid DeleteRequest deleteRequest,
                                                 HttpServletRequest httpServletRequest) {
        Image image = imageService.getById(deleteRequest.getId());
        ThrowUtils.throwIf(image == null, BusinessCodeEnum.NOT_FOUND_ERROR);
        User loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(imageService.deleteImage(image, loginUser));
    }

    /**
     * 获取图片标签/分类列表
     *
     * @return
     */
    @GetMapping("/tagList")
    public BusinessResponse<ImageTagCategory> listImageTagCategory() {
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");

        return ResultUtils.success(new ImageTagCategory(tagList, categoryList));
    }

    /**
     * 管理员根据id获取图片
     */
    @GetMapping("/{id}")
    @AuthCheck(mustRole = Constants.ADMIN_ROLE)
    public BusinessResponse<Image> getImageById(@PathVariable long id, HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(doGetImage(id, loginUser));
    }

    /**
     * 管理员分页获取图片列表
     */
    @PostMapping("/page")
    @AuthCheck(mustRole = Constants.ADMIN_ROLE)
    public BusinessResponse<Page<Image>> listImageByPage(@RequestBody ImageQueryRequest imageQueryRequest, HttpServletRequest httpServletRequest) {
        return ResultUtils.success(doListImage(imageQueryRequest, userService.getLoginUser(httpServletRequest)));
    }

    /**
     * 管理员审核图片
     *
     * @param imageReviewRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = Constants.ADMIN_ROLE)
    public BusinessResponse<Boolean> reviewImage(@RequestBody @Valid ImageReviewRequest imageReviewRequest,
                                                 HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        imageService.reviewImage(imageReviewRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 管理员抓取图片
     *
     * @param imageFetchRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/fetch")
    @AuthCheck(mustRole = Constants.ADMIN_ROLE)
    public BusinessResponse<Integer> fetchImage(@RequestBody @Valid ImageFetchRequest imageFetchRequest,
                                                HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(imageService.fetchImage(imageFetchRequest, loginUser));
    }

    private Image doGetImage(long id, User loginUser) {
        ThrowUtils.throwIf(id <= 0, BusinessCodeEnum.PARAMS_ERROR);
        Image image = imageService.getById(id);
        ThrowUtils.throwIf(image == null, BusinessCodeEnum.NOT_FOUND_ERROR);
        if (image.getSpaceId() != null) {
            // 私有空间
            imageService.validateImageAuth(image, loginUser);
        }
        return image;
    }

    private Page<Image> doListImage(ImageQueryRequest imageQueryRequest, User loginUser) {
        if (imageQueryRequest.getSpaceId() != null) {
            Space space = spaceService.getById(imageQueryRequest.getSpaceId());
            ThrowUtils.throwIf(space == null, BusinessCodeEnum.NOT_FOUND_ERROR, "空间不存在");
            ThrowUtils.throwIf(!space.getUserId().equals(loginUser.getId()), BusinessCodeEnum.NO_AUTH_ERROR, "没有空间权限");
        }
        long current = imageQueryRequest.getCurrent();
        long size = imageQueryRequest.getPageSize();

        QueryWrapper<Image> queryWrapper = imageService.getQueryWrapper(imageQueryRequest);

        String imageColor = imageQueryRequest.getImageColor();
        if (imageColor != null) {
            // 走按颜色搜索逻辑,内存分页
            queryWrapper.isNotNull("image_color");
            List<Image> imageList = imageService.list(queryWrapper);
            if (CollUtil.isEmpty(imageList)) {
                return new Page<>(current, size, 0);
            }
            // 色调字符串转换为 Color 对象
            Color targetColor = Color.decode(imageColor);
            // 按颜色相似度排序
            List<Image> sortedImageList = imageList.stream()
                    .sorted(Comparator.<Image>comparingDouble(image -> {
                        Color color = Color.decode(image.getImageColor());
                        return ColorSimilarUtils.calculateSimilarity(color, targetColor);
                    }).reversed()).collect(Collectors.toList());

            // 手动分页
            int total = sortedImageList.size();
            int start = (int) ((current - 1) * size);
            int end = (int) Math.min(start + size, total);
            if (start >= total) {
                Page<Image> emptyPage = new Page<>(current, size, total);
                emptyPage.setRecords(new ArrayList<>());
                return emptyPage;
            }
            Page<Image> resultPage = new Page<>(current, size, total);
            resultPage.setRecords(sortedImageList.subList(start, end));
            return resultPage;
        }
        return imageService.page(new Page<>(current, size), queryWrapper);
    }

}
