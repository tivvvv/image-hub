package com.tiv.image.hub.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tiv.image.hub.common.BusinessCodeEnum;
import com.tiv.image.hub.exception.BusinessException;
import com.tiv.image.hub.manager.CosManager;
import com.tiv.image.hub.manager.upload.FileImageUpload;
import com.tiv.image.hub.manager.upload.UrlImageUpload;
import com.tiv.image.hub.mapper.ImageMapper;
import com.tiv.image.hub.mapper.SpaceMapper;
import com.tiv.image.hub.model.dto.image.request.*;
import com.tiv.image.hub.model.dto.image.result.ImageUploadResult;
import com.tiv.image.hub.model.entity.Image;
import com.tiv.image.hub.model.entity.Space;
import com.tiv.image.hub.model.entity.User;
import com.tiv.image.hub.model.enums.ImageReviewStatusEnum;
import com.tiv.image.hub.model.vo.ImageVO;
import com.tiv.image.hub.model.vo.UserVO;
import com.tiv.image.hub.service.ImageService;
import com.tiv.image.hub.service.UserService;
import com.tiv.image.hub.util.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ImageServiceImpl extends ServiceImpl<ImageMapper, Image> implements ImageService {

    @Resource
    private FileImageUpload fileImageUpload;

    @Resource
    private UrlImageUpload urlImageUpload;

    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;

    @Resource
    private SpaceMapper spaceMapper;

    @Resource
    private TransactionTemplate transactionTemplate;

    private static final int URL_MAX_LENGTH = 512;

    private static final int INTRO_MAX_LENGTH = 512;

    private static final String FETCH_URL = "https://cn.bing.com/images/async?q=%s&mmasync=1";

    private static final String DG_CONTROL = "dgControl";

    private static final String IMG_MIMG = "img.mimg";

    private static final String SRC = "src";

    @Override
    public void validateImage(Image image) {
        ThrowUtils.throwIf(image == null, BusinessCodeEnum.PARAMS_ERROR);
        ThrowUtils.throwIf(image.getId() == null, BusinessCodeEnum.PARAMS_ERROR, "id不能为空");

        String url = image.getImageUrl();
        String intro = image.getImageIntro();

        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > URL_MAX_LENGTH, BusinessCodeEnum.PARAMS_ERROR, "url过长");
        }
        if (StrUtil.isNotBlank(intro)) {
            ThrowUtils.throwIf(intro.length() > INTRO_MAX_LENGTH, BusinessCodeEnum.PARAMS_ERROR, "简介过长");
        }
    }

    @Override
    public ImageVO uploadImage(Object inputSource, ImageUploadRequest imageUploadRequest, User loginUser) {
        Long imageId = imageUploadRequest.getId();
        Long spaceId = imageUploadRequest.getSpaceId();

        if (spaceId != null) {
            // 校验空间是否存在
            Space space = spaceMapper.selectById(spaceId);
            ThrowUtils.throwIf(space == null, BusinessCodeEnum.NOT_FOUND_ERROR, "空间不存在");
            // 校验空间权限,仅空间所有者可以上传
            ThrowUtils.throwIf(!loginUser.getId().equals(space.getUserId()), BusinessCodeEnum.NO_AUTH_ERROR);
            // 校验空间容量
            ThrowUtils.throwIf(space.getCurrentSize() >= space.getMaxSize(), BusinessCodeEnum.OPERATION_ERROR, "可用空间容量不足");
            ThrowUtils.throwIf(space.getCurrentCount() >= space.getMaxCount(), BusinessCodeEnum.OPERATION_ERROR, "可用图片数量不足");
        }

        Image existedImage = null;
        if (imageId != null) {
            // 更新图片,需校验图片是否存在
            existedImage = this.getById(imageId);
            ThrowUtils.throwIf(existedImage == null, BusinessCodeEnum.NOT_FOUND_ERROR, "图片不存在");
            // 仅创建人和管理员可更新图片
            ThrowUtils.throwIf(!loginUser.getId().equals(existedImage.getUserId())
                    && !userService.isAdmin(loginUser), BusinessCodeEnum.NO_AUTH_ERROR);
        }

        // 创建目录
        String uploadPathPrefix;
        if (spaceId == null) {
            // 公共空间使用用户id作为目录
            uploadPathPrefix = String.format("public/%s", loginUser.getId());
        } else {
            // 私有空间使用空间id作为目录
            uploadPathPrefix = String.format("space/%s", spaceId);
        }

        // 上传图片
        ImageUploadResult imageUploadResult = null;
        if (inputSource instanceof MultipartFile) {
            imageUploadResult = fileImageUpload.uploadImage((MultipartFile) inputSource, uploadPathPrefix);
        } else if (inputSource instanceof String) {
            imageUploadResult = urlImageUpload.uploadImage((String) inputSource, uploadPathPrefix);
        } else {
            throw new BusinessException(BusinessCodeEnum.PARAMS_ERROR, "图片类型不支持");
        }

        Image image = new Image();
        BeanUtil.copyProperties(imageUploadResult, image);
        image.setUserId(loginUser.getId());
        image.setSpaceId(spaceId);

        // 优先使用指定的图片名称
        if (StrUtil.isNotBlank(imageUploadRequest.getImageName())) {
            image.setImageName(imageUploadRequest.getImageName());
        }

        if (existedImage != null) {
            image.setId(existedImage.getId());
            clearImageFile(existedImage);
        }
        // 补充审核参数
        this.populateReviewParams(image, loginUser);

        // 更新库表
        long existedImageSize = existedImage == null ? 0 : existedImage.getImageSize();
        long existedImageCount = existedImage == null ? 0 : 1;

        transactionTemplate.execute(status -> {
            boolean result = this.saveOrUpdate(image);
            ThrowUtils.throwIf(!result, BusinessCodeEnum.SYSTEM_ERROR, "保存图片失败");
            if (spaceId != null) {
                // 更新空间已使用容量和数量
                LambdaUpdateWrapper<Space> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(Space::getId, spaceId)
                        .setSql("current_size = current_size + " + (image.getImageSize() - existedImageSize))
                        .setSql("current_count = current_count + " + (1 - existedImageCount));
                int updated = spaceMapper.update(null, updateWrapper);
                ThrowUtils.throwIf(updated < 1, BusinessCodeEnum.SYSTEM_ERROR, "更新空间额度失败");
            }
            return true;
        });

        return ImageVO.transferToVO(image);
    }

    @Override
    public ImageVO getImageVO(Image image) {
        ImageVO imageVO = ImageVO.transferToVO(image);
        Long userId = image.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            imageVO.setUserVO(userVO);
        }
        return imageVO;
    }

    @Override
    public Page<ImageVO> getImageVOPage(Page<Image> imagePage) {
        List<Image> imageList = imagePage.getRecords();
        Page<ImageVO> imageVOPage = new Page<>(imagePage.getCurrent(), imagePage.getSize(), imagePage.getTotal());
        if (CollUtil.isEmpty(imageList)) {
            return imageVOPage;
        }
        // 转换成包装类
        List<ImageVO> imageVOList = imageList.stream()
                .map(ImageVO::transferToVO)
                .collect(Collectors.toList());
        Set<Long> userIdSet = imageList.stream()
                .map(Image::getUserId)
                .collect(Collectors.toSet());
        // 查询用户信息
        List<User> userList = userService.listByIds(userIdSet);
        // 用户id -> 用户信息的映射
        Map<Long, User> userIdToUserMap = userList.stream()
                .collect(Collectors.toMap(
                        User::getId,
                        user -> user,
                        (existing, replacement) -> replacement)
                );
        // 添加用户信息
        imageVOList.forEach(imageVO -> {
            Long userId = imageVO.getUserId();
            if (userIdToUserMap.containsKey(userId)) {
                User user = userIdToUserMap.get(userId);
                imageVO.setUserVO(userService.getUserVO(user));
            }
        });
        imageVOPage.setRecords(imageVOList);
        return imageVOPage;
    }

    @Override
    public QueryWrapper<Image> getQueryWrapper(ImageQueryRequest imageQueryRequest) {
        ThrowUtils.throwIf(imageQueryRequest == null, BusinessCodeEnum.PARAMS_ERROR, "请求参数为空");
        QueryWrapper<Image> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq(imageQueryRequest.getId() != null, "id", imageQueryRequest.getId());
        queryWrapper.like(StrUtil.isNotBlank(imageQueryRequest.getImageName()), "image_name", imageQueryRequest.getImageName());
        queryWrapper.like(StrUtil.isNotBlank(imageQueryRequest.getImageIntro()), "image_intro", imageQueryRequest.getImageIntro());
        queryWrapper.eq(StrUtil.isNotBlank(imageQueryRequest.getImageCategory()), "image_category", imageQueryRequest.getImageCategory());
        queryWrapper.eq(imageQueryRequest.getImageSize() != null, "image_size", imageQueryRequest.getImageSize());
        queryWrapper.eq(imageQueryRequest.getImageWidth() != null, "image_width", imageQueryRequest.getImageWidth());
        queryWrapper.eq(imageQueryRequest.getImageHeight() != null, "image_height", imageQueryRequest.getImageHeight());
        queryWrapper.eq(imageQueryRequest.getImageScale() != null, "image_scale", imageQueryRequest.getImageScale());
        queryWrapper.eq(StrUtil.isNotBlank(imageQueryRequest.getImageFormat()), "image_format", imageQueryRequest.getImageFormat());
        queryWrapper.eq(imageQueryRequest.getUserId() != null, "user_id", imageQueryRequest.getUserId());
        queryWrapper.eq(ObjUtil.isNotEmpty(imageQueryRequest.getReviewStatus()), "review_status", imageQueryRequest.getReviewStatus());
        queryWrapper.eq(ObjUtil.isNotEmpty(imageQueryRequest.getReviewerId()), "reviewer_id", imageQueryRequest.getReviewerId());
        queryWrapper.ge(imageQueryRequest.getUpdateTimeStart() != null, "update_time", imageQueryRequest.getUpdateTimeStart());
        queryWrapper.le(imageQueryRequest.getUpdateTimeEnd() != null, "update_time", imageQueryRequest.getUpdateTimeEnd());
        // 处理json格式的imageTags
        if (CollectionUtil.isNotEmpty(imageQueryRequest.getImageTagList())) {
            for (String tag : imageQueryRequest.getImageTagList()) {
                queryWrapper.like("image_tags", "\"" + tag + "\"");
            }
        }
        // 处理关键字查询条件
        String keyword = imageQueryRequest.getKeyword();
        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.and(wrapper -> wrapper.like("image_name", keyword)
                    .or().like("image_intro", keyword)
                    .or().like("image_tags", keyword)
                    .or().like("image_category", keyword)
            );
        }
        // 处理空间
        if (imageQueryRequest.getSpaceId() == null) {
            // 只查询公共空间的图片
            queryWrapper.isNull(true, "space_id");
        } else {
            // 查询指定空间
            queryWrapper.eq("space_id", imageQueryRequest.getSpaceId());
        }
        queryWrapper.orderBy(StrUtil.isNotBlank(imageQueryRequest.getSortField()), "asc".equals(imageQueryRequest.getSortOrder()), imageQueryRequest.getSortField());

        return queryWrapper;
    }

    @Override
    public void reviewImage(ImageReviewRequest imageReviewRequest, User loginUser) {
        // 校验参数
        Integer reviewStatusValue = imageReviewRequest.getReviewStatus();
        ImageReviewStatusEnum imageReviewStatusEnum = ImageReviewStatusEnum.getEnumByValue(reviewStatusValue);
        ThrowUtils.throwIf(imageReviewStatusEnum == null || imageReviewStatusEnum == ImageReviewStatusEnum.REVIEWING,
                BusinessCodeEnum.PARAMS_ERROR, "审核状态错误");
        // 校验图片是否存在
        Image image = this.getById(imageReviewRequest.getId());
        ThrowUtils.throwIf(image == null, BusinessCodeEnum.NOT_FOUND_ERROR, "图片不存在");
        // 审核状态不应重复
        ThrowUtils.throwIf(image.getReviewStatus() == imageReviewStatusEnum.value, BusinessCodeEnum.PARAMS_ERROR, "重复审核");

        Image updateImage = new Image();
        BeanUtil.copyProperties(imageReviewRequest, updateImage);
        updateImage.setReviewerId(loginUser.getId());
        updateImage.setReviewTime(new Date());

        // 更新库表
        boolean result = this.updateById(updateImage);
        ThrowUtils.throwIf(!result, BusinessCodeEnum.OPERATION_ERROR, "图片审核失败");
    }

    @Override
    public void populateReviewParams(Image image, User loginUser) {
        if (userService.isAdmin(loginUser)) {
            // 管理员自动过审
            image.setReviewStatus(ImageReviewStatusEnum.PASS.value);
            image.setReviewMessage("管理员自动过审");
            image.setReviewerId(loginUser.getId());
            image.setReviewTime(new Date());
        } else {
            image.setReviewStatus(ImageReviewStatusEnum.REVIEWING.value);
        }
    }

    @Override
    public Integer fetchImage(ImageFetchRequest imageFetchRequest, User loginUser) {
        // 抓取图片地址
        String fetchUrl = String.format(FETCH_URL, imageFetchRequest.getSearchText());
        Document document = null;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            throw new BusinessException(BusinessCodeEnum.OPERATION_ERROR, "抓取图片失败");
        }

        // 解析内容
        Element div = document.getElementsByClass(DG_CONTROL).first();
        if (ObjUtil.isEmpty(div)) {
            throw new BusinessException(BusinessCodeEnum.OPERATION_ERROR, "获取图片元素失败");
        }
        Elements imgElements = div.select(IMG_MIMG);

        String imageNamePrefix = imageFetchRequest.getImageNamePrefix();
        // 默认使用搜索词作为图片名称前缀
        if (StrUtil.isBlank(imageNamePrefix)) {
            imageNamePrefix = imageFetchRequest.getSearchText();
        }

        String now = DateUtil.now();
        int uploadCount = 0;
        for (Element imgElement : imgElements) {
            String imgSrc = imgElement.attr(SRC);
            if (StrUtil.isBlank(imgSrc)) {
                continue;
            }
            // 处理图片地址
            int index = imgSrc.indexOf('?');
            if (index > -1) {
                imgSrc = imgSrc.substring(0, index);
            }
            ImageUploadRequest imageUploadRequest = new ImageUploadRequest();
            imageUploadRequest.setFileUrl(imgSrc);
            String imageName = String.format("%s_%s_%s", imageNamePrefix, now, uploadCount + 1);
            imageUploadRequest.setImageName(imageName);
            try {
                ImageVO imageVO = this.uploadImage(imgSrc, imageUploadRequest, loginUser);
                uploadCount++;
                log.info("上传图片成功:{}", imageVO);
            } catch (Exception e) {
                log.error("上传图片失败:{}", e.getMessage());
            }
            if (uploadCount >= imageFetchRequest.getFetchSize()) {
                break;
            }
        }
        return uploadCount;
    }

    @Async
    @Override
    public void clearImageFile(Image image) {
        String imageUrl = image.getImageUrl();
        // 判断图片文件是否被多条记录使用
        Long count = this.lambdaQuery()
                .eq(Image::getImageUrl, imageUrl)
                .count();
        if (count > 1) {
            return;
        }
        // 删除原图
        cosManager.deleteObject(imageUrl);
        // 删除缩略图
        String thumbnailUrl = image.getThumbnailUrl();
        if (StrUtil.isNotBlank(thumbnailUrl)) {
            cosManager.deleteObject(thumbnailUrl);
        }
    }

    @Override
    public void validateImageAuth(Image image, User loginUser) {
        Long spaceId = image.getSpaceId();
        Long userId = loginUser.getId();
        if (spaceId == null) {
            // 公共空间,图片创建人和管理员可操作
            ThrowUtils.throwIf(!userId.equals(image.getUserId())
                    && !userService.isAdmin(loginUser), BusinessCodeEnum.NO_AUTH_ERROR);
        } else {
            // 私有空间,仅图片创建人可操作
            ThrowUtils.throwIf(!userId.equals(image.getUserId()), BusinessCodeEnum.NO_AUTH_ERROR);
        }
    }

    @Override
    public Boolean deleteImage(Image image, User loginUser) {
        // 校验权限
        validateImageAuth(image, loginUser);

        transactionTemplate.execute(status -> {
            boolean result = removeById(image.getId());
            ThrowUtils.throwIf(!result, BusinessCodeEnum.OPERATION_ERROR);

            if (image.getSpaceId() != null) {
                // 更新空间已使用容量和数量
                LambdaUpdateWrapper<Space> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(Space::getId, image.getSpaceId())
                        .setSql("current_size = current_size - " + image.getImageSize())
                        .setSql("current_count = current_count - 1");
                int updated = spaceMapper.update(null, updateWrapper);
                ThrowUtils.throwIf(updated < 1, BusinessCodeEnum.SYSTEM_ERROR, "更新空间额度失败");
            }
            return true;
        });

        // 清理图片文件
        clearImageFile(image);
        return true;
    }

    @Override
    public Boolean updateImage(ImageUpdateRequest imageUpdateRequest, User loginUser) {
        // 判断图片是否存在
        long id = imageUpdateRequest.getId();
        Image oldImage = getById(id);
        ThrowUtils.throwIf(oldImage == null, BusinessCodeEnum.NOT_FOUND_ERROR);

        // 校验权限
        validateImageAuth(oldImage, loginUser);

        Image image = new Image();
        BeanUtils.copyProperties(imageUpdateRequest, image);
        image.setImageTags(JSONUtil.toJsonStr(imageUpdateRequest.getImageTagList()));

        // 校验图片参数
        validateImage(image);

        // 填充审核参数
        populateReviewParams(image, loginUser);

        // 更新库表
        boolean result = updateById(image);
        ThrowUtils.throwIf(!result, BusinessCodeEnum.OPERATION_ERROR);
        return true;
    }

    @Override
    public Boolean batchUpdateImage(ImageBatchUpdateRequest imageBatchUpdateRequest, User loginUser) {
        // 1. 校验空间权限
        Long spaceId = imageBatchUpdateRequest.getSpaceId();
        Space space = spaceMapper.selectById(spaceId);
        ThrowUtils.throwIf(space == null, BusinessCodeEnum.NOT_FOUND_ERROR, "空间不存在");
        ThrowUtils.throwIf(!space.getUserId().equals(loginUser.getId()), BusinessCodeEnum.NO_AUTH_ERROR, "没有空间权限");

        // 2. 查询图片
        List<Image> imageList = this.lambdaQuery()
                .select(Image::getId, Image::getImageName)
                .in(Image::getId, imageBatchUpdateRequest.getImageIds())
                .eq(Image::getSpaceId, spaceId)
                .list();
        ThrowUtils.throwIf(CollUtil.isEmpty(imageList), BusinessCodeEnum.NOT_FOUND_ERROR, "图片不存在");

        // 3. 批量更新图片分类和标签
        String imageCategory = imageBatchUpdateRequest.getImageCategory();
        List<String> imageTagList = imageBatchUpdateRequest.getImageTagList();
        imageList.forEach(image -> {
            if (StrUtil.isNotBlank(imageCategory)) {
                image.setImageCategory(imageCategory);
            }
            if (CollUtil.isNotEmpty(imageTagList)) {
                image.setImageTags(JSONUtil.toJsonStr(imageTagList));
            }
        });
        return transactionTemplate.execute(status -> {
            boolean result = this.updateBatchById(imageList);
            ThrowUtils.throwIf(!result, BusinessCodeEnum.OPERATION_ERROR);
            return true;
        });
    }

}