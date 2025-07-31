package com.tiv.image.hub.controller;

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
import com.tiv.image.hub.model.dto.picture.*;
import com.tiv.image.hub.model.entity.Picture;
import com.tiv.image.hub.model.entity.User;
import com.tiv.image.hub.model.enums.PictureReviewStatusEnum;
import com.tiv.image.hub.model.enums.UserRoleEnum;
import com.tiv.image.hub.model.vo.PictureTagCategory;
import com.tiv.image.hub.model.vo.PictureVO;
import com.tiv.image.hub.service.PictureService;
import com.tiv.image.hub.service.UserService;
import com.tiv.image.hub.util.ResultUtils;
import com.tiv.image.hub.util.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 图片controller
 */
@Slf4j
@RestController
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private PictureService pictureService;

    @Resource
    private UserService userService;

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
     * @param pictureUploadRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/upload")
    public BusinessResponse<PictureVO> uploadPicture(@RequestPart("file") MultipartFile multipartFile,
                                                     PictureUploadRequest pictureUploadRequest,
                                                     HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser));
    }

    /**
     * 根据url上传图片
     *
     * @param pictureUploadRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/upload/url")
    public BusinessResponse<PictureVO> uploadPictureByUrl(@RequestBody PictureUploadRequest pictureUploadRequest,
                                                          HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        String fileUrl = pictureUploadRequest.getFileUrl();
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), BusinessCodeEnum.PARAMS_ERROR, "文件url不能为空");
        return ResultUtils.success(pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser));
    }

    /**
     * 更新图片
     *
     * @param pictureUpdateRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/update")
    public BusinessResponse<Boolean> updatePicture(@RequestBody @Valid PictureUpdateRequest pictureUpdateRequest,
                                                   HttpServletRequest httpServletRequest) {

        // 判断图片是否存在
        long id = pictureUpdateRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, BusinessCodeEnum.NOT_FOUND_ERROR);

        User loginUser = userService.getLoginUser(httpServletRequest);
        // 仅创建人或管理员可更新图片
        ThrowUtils.throwIf(!loginUser.getId().equals(oldPicture.getUserId())
                && !UserRoleEnum.ADMIN.value.equals(loginUser.getUserRole()), BusinessCodeEnum.NO_AUTH_ERROR);

        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUpdateRequest, picture);
        picture.setPicTags(JSONUtil.toJsonStr(pictureUpdateRequest.getPicTagList()));

        // 校验图片参数
        pictureService.validatePicture(picture);

        // 补充审核参数
        pictureService.fillReviewParams(picture, loginUser);

        // 更新库表
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, BusinessCodeEnum.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据id获取图片视图
     */
    @GetMapping("/vo/{id}")
    public BusinessResponse<PictureVO> getPictureVOById(@PathVariable long id) {
        Picture picture = doGetPicture(id);
        // 用户只能查询审核通过的图片
        ThrowUtils.throwIf(picture.getReviewStatus() != PictureReviewStatusEnum.PASS.value,
                BusinessCodeEnum.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(pictureService.getPictureVO(picture));
    }

    /**
     * 分页获取图片视图列表
     */
    @PostMapping("/page/vo")
    public BusinessResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        ThrowUtils.throwIf(pictureQueryRequest.getPageSize() > USER_QUERY_PICTURE_LIMIT,
                BusinessCodeEnum.PARAMS_ERROR, "查询数量过多");
        // 用户只能查询审核通过的图片
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.value);
        Page<Picture> picturePage = doListPicture(pictureQueryRequest);
        // 获取封装类
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage));
    }

    /**
     * 分页获取图片视图列表(带缓存)
     */
    @PostMapping("/page/vo/cache")
    public BusinessResponse<Page<PictureVO>> listPictureVOByPageWithCache(@RequestBody PictureQueryRequest pictureQueryRequest) {
        ThrowUtils.throwIf(pictureQueryRequest.getPageSize() > USER_QUERY_PICTURE_LIMIT,
                BusinessCodeEnum.PARAMS_ERROR, "查询数量过多");
        // 用户只能查询审核通过的图片
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.value);

        // 1. 查询本地缓存
        String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
        String hashKey = DigestUtil.md5Hex(queryCondition.getBytes());
        // 缓存key格式: 项目名:方法名:md5
        String cacheKey = String.format("%s:%s:%s", Constants.PROJECT, "listPictureVOByPageWithCache", hashKey);
        String cachedValue = localCache.getIfPresent(cacheKey);
        if (StrUtil.isNotBlank(cachedValue)) {
            // 1.1 本地缓存命中,直接返回结果
            Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
            return ResultUtils.success(cachedPage);
        }
        // 2. 本地缓存未命中,查询redis缓存
        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
        cachedValue = opsForValue.get(cacheKey);
        if (StrUtil.isNotBlank(cachedValue)) {
            // 2.1 redis缓存命中,更新本地缓存
            localCache.put(cacheKey, cachedValue);
            Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
            return ResultUtils.success(cachedPage);
        }

        // 3. 多级缓存都未命中,查询数据库
        Page<Picture> picturePage = doListPicture(pictureQueryRequest);
        // 获取封装类
        Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage);

        // 4. 更新本地缓存
        localCache.put(cacheKey, JSONUtil.toJsonStr(pictureVOPage));

        // 5. 更新redis缓存
        // 缓存时间5-10min,避免缓存雪崩
        int expireTime = 300 + RandomUtil.randomInt(0, 300);
        opsForValue.set(cacheKey, JSONUtil.toJsonStr(pictureVOPage), expireTime, TimeUnit.SECONDS);

        return ResultUtils.success(pictureVOPage);
    }

    /**
     * 删除图片
     *
     * @param deleteRequest
     * @param httpServletRequest
     * @return
     */
    @DeleteMapping()
    public BusinessResponse<Boolean> deletePicture(@RequestBody @Valid DeleteRequest deleteRequest,
                                                   HttpServletRequest httpServletRequest) {
        Picture picture = pictureService.getById(deleteRequest.getId());
        ThrowUtils.throwIf(picture == null, BusinessCodeEnum.NOT_FOUND_ERROR);

        User loginUser = userService.getLoginUser(httpServletRequest);
        // 仅创建人或管理员可删除
        ThrowUtils.throwIf(!loginUser.getId().equals(picture.getUserId())
                && !UserRoleEnum.ADMIN.value.equals(loginUser.getUserRole()), BusinessCodeEnum.NO_AUTH_ERROR);
        boolean result = pictureService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result, BusinessCodeEnum.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 获取图片标签/分类列表
     *
     * @return
     */
    @GetMapping("/tagList")
    public BusinessResponse<PictureTagCategory> listPictureTagCategory() {
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");

        return ResultUtils.success(new PictureTagCategory(tagList, categoryList));
    }

    /**
     * 管理员根据id获取图片
     */
    @GetMapping("/{id}")
    @AuthCheck(mustRole = Constants.ADMIN_ROLE)
    public BusinessResponse<Picture> getPictureById(@PathVariable long id) {
        return ResultUtils.success(doGetPicture(id));
    }

    /**
     * 管理员分页获取图片列表
     */
    @PostMapping("/page")
    @AuthCheck(mustRole = Constants.ADMIN_ROLE)
    public BusinessResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        return ResultUtils.success(doListPicture(pictureQueryRequest));
    }

    /**
     * 管理员审核图片
     *
     * @param pictureReviewRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = Constants.ADMIN_ROLE)
    public BusinessResponse<Boolean> reviewPicture(@RequestBody @Valid PictureReviewRequest pictureReviewRequest,
                                                   HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        pictureService.reviewPicture(pictureReviewRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 管理员抓取图片
     *
     * @param pictureFetchRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/fetch")
    @AuthCheck(mustRole = Constants.ADMIN_ROLE)
    public BusinessResponse<Integer> fetchPicture(@RequestBody @Valid PictureFetchRequest pictureFetchRequest,
                                                  HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(pictureService.fetchPicture(pictureFetchRequest, loginUser));
    }

    private Picture doGetPicture(long id) {
        ThrowUtils.throwIf(id <= 0, BusinessCodeEnum.PARAMS_ERROR);
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, BusinessCodeEnum.NOT_FOUND_ERROR);
        return picture;
    }

    private Page<Picture> doListPicture(PictureQueryRequest pictureQueryRequest) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();

        QueryWrapper<Picture> queryWrapper = pictureService.getQueryWrapper(pictureQueryRequest);
        return pictureService.page(new Page<>(current, size), queryWrapper);
    }

}
