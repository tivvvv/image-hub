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
import com.tiv.image.hub.manager.upload.FilePictureUpload;
import com.tiv.image.hub.manager.upload.UrlPictureUpload;
import com.tiv.image.hub.mapper.PictureMapper;
import com.tiv.image.hub.mapper.SpaceMapper;
import com.tiv.image.hub.model.dto.picture.*;
import com.tiv.image.hub.model.entity.Picture;
import com.tiv.image.hub.model.entity.Space;
import com.tiv.image.hub.model.entity.User;
import com.tiv.image.hub.model.enums.PictureReviewStatusEnum;
import com.tiv.image.hub.model.vo.PictureVO;
import com.tiv.image.hub.model.vo.UserVO;
import com.tiv.image.hub.service.PictureService;
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
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {

    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private UrlPictureUpload urlPictureUpload;

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
    public void validatePicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, BusinessCodeEnum.PARAMS_ERROR);
        ThrowUtils.throwIf(picture.getId() == null, BusinessCodeEnum.PARAMS_ERROR, "id不能为空");

        String url = picture.getPicUrl();
        String intro = picture.getPicIntro();

        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > URL_MAX_LENGTH, BusinessCodeEnum.PARAMS_ERROR, "url过长");
        }
        if (StrUtil.isNotBlank(intro)) {
            ThrowUtils.throwIf(intro.length() > INTRO_MAX_LENGTH, BusinessCodeEnum.PARAMS_ERROR, "简介过长");
        }
    }

    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        Long pictureId = pictureUploadRequest.getId();
        Long spaceId = pictureUploadRequest.getSpaceId();

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

        Picture existedPicture = null;
        if (pictureId != null) {
            // 更新图片,需校验图片是否存在
            existedPicture = this.getById(pictureId);
            ThrowUtils.throwIf(existedPicture == null, BusinessCodeEnum.NOT_FOUND_ERROR, "图片不存在");
            // 仅创建人和管理员可更新图片
            ThrowUtils.throwIf(!loginUser.getId().equals(existedPicture.getUserId())
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
        PictureUploadResult pictureUploadResult = null;
        if (inputSource instanceof MultipartFile) {
            pictureUploadResult = filePictureUpload.uploadPicture((MultipartFile) inputSource, uploadPathPrefix);
        } else if (inputSource instanceof String) {
            pictureUploadResult = urlPictureUpload.uploadPicture((String) inputSource, uploadPathPrefix);
        } else {
            throw new BusinessException(BusinessCodeEnum.PARAMS_ERROR, "图片类型不支持");
        }

        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureUploadResult, picture);
        picture.setUserId(loginUser.getId());
        picture.setSpaceId(spaceId);

        // 优先使用指定的图片名称
        if (StrUtil.isNotBlank(pictureUploadRequest.getPicName())) {
            picture.setPicName(pictureUploadRequest.getPicName());
        }

        if (existedPicture != null) {
            picture.setId(existedPicture.getId());
            clearPictureFile(existedPicture);
        }
        // 补充审核参数
        this.populateReviewParams(picture, loginUser);

        // 更新库表
        long existedPictureSize = existedPicture == null ? 0 : existedPicture.getPicSize();
        long existedPictureCount = existedPicture == null ? 0 : 1;

        transactionTemplate.execute(status -> {
            boolean result = this.saveOrUpdate(picture);
            ThrowUtils.throwIf(!result, BusinessCodeEnum.SYSTEM_ERROR, "保存图片失败");
            if (spaceId != null) {
                // 更新空间已使用容量和数量
                LambdaUpdateWrapper<Space> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(Space::getId, spaceId)
                        .setSql("current_size = current_size + " + (picture.getPicSize() - existedPictureSize))
                        .setSql("current_count = current_count + " + (1 - existedPictureCount));
                int updated = spaceMapper.update(null, updateWrapper);
                ThrowUtils.throwIf(updated < 1, BusinessCodeEnum.SYSTEM_ERROR, "更新空间额度失败");
            }
            return true;
        });

        return PictureVO.transferToVO(picture);
    }

    @Override
    public PictureVO getPictureVO(Picture picture) {
        PictureVO pictureVO = PictureVO.transferToVO(picture);
        Long userId = picture.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUserVO(userVO);
        }
        return pictureVO;
    }

    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        // 转换成包装类
        List<PictureVO> pictureVOList = pictureList.stream()
                .map(PictureVO::transferToVO)
                .collect(Collectors.toList());
        Set<Long> userIdSet = pictureList.stream()
                .map(Picture::getUserId)
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
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            if (userIdToUserMap.containsKey(userId)) {
                User user = userIdToUserMap.get(userId);
                pictureVO.setUserVO(userService.getUserVO(user));
            }
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        ThrowUtils.throwIf(pictureQueryRequest == null, BusinessCodeEnum.PARAMS_ERROR, "请求参数为空");
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq(pictureQueryRequest.getId() != null, "id", pictureQueryRequest.getId());
        queryWrapper.like(StrUtil.isNotBlank(pictureQueryRequest.getPicName()), "pic_name", pictureQueryRequest.getPicName());
        queryWrapper.like(StrUtil.isNotBlank(pictureQueryRequest.getPicIntro()), "pic_intro", pictureQueryRequest.getPicIntro());
        queryWrapper.eq(StrUtil.isNotBlank(pictureQueryRequest.getPicCategory()), "pic_category", pictureQueryRequest.getPicCategory());
        queryWrapper.eq(pictureQueryRequest.getPicSize() != null, "pic_size", pictureQueryRequest.getPicSize());
        queryWrapper.eq(pictureQueryRequest.getPicWidth() != null, "pic_width", pictureQueryRequest.getPicWidth());
        queryWrapper.eq(pictureQueryRequest.getPicHeight() != null, "pic_height", pictureQueryRequest.getPicHeight());
        queryWrapper.eq(pictureQueryRequest.getPicScale() != null, "pic_scale", pictureQueryRequest.getPicScale());
        queryWrapper.eq(StrUtil.isNotBlank(pictureQueryRequest.getPicFormat()), "pic_format", pictureQueryRequest.getPicFormat());
        queryWrapper.eq(pictureQueryRequest.getUserId() != null, "user_id", pictureQueryRequest.getUserId());
        queryWrapper.eq(ObjUtil.isNotEmpty(pictureQueryRequest.getReviewStatus()), "review_status", pictureQueryRequest.getReviewStatus());
        queryWrapper.eq(ObjUtil.isNotEmpty(pictureQueryRequest.getReviewerId()), "reviewer_id", pictureQueryRequest.getReviewerId());
        queryWrapper.ge(pictureQueryRequest.getUpdateTimeStart() != null, "update_time", pictureQueryRequest.getUpdateTimeStart());
        queryWrapper.le(pictureQueryRequest.getUpdateTimeEnd() != null, "update_time", pictureQueryRequest.getUpdateTimeEnd());
        // 处理json格式的picTags
        if (CollectionUtil.isNotEmpty(pictureQueryRequest.getPicTagList())) {
            for (String tag : pictureQueryRequest.getPicTagList()) {
                queryWrapper.like("pic_tags", "\"" + tag + "\"");
            }
        }
        // 处理关键字查询条件
        String keyword = pictureQueryRequest.getKeyword();
        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.and(wrapper -> wrapper.like("pic_name", keyword)
                    .or().like("pic_intro", keyword)
                    .or().like("pic_tags", keyword)
                    .or().like("pic_category", keyword)
            );
        }
        // 处理空间
        if (pictureQueryRequest.getSpaceId() == null) {
            // 只查询公共空间的图片
            queryWrapper.isNull(true, "space_id");
        } else {
            // 查询指定空间
            queryWrapper.eq("space_id", pictureQueryRequest.getSpaceId());
        }
        queryWrapper.orderBy(StrUtil.isNotBlank(pictureQueryRequest.getSortField()), "asc".equals(pictureQueryRequest.getSortOrder()), pictureQueryRequest.getSortField());

        return queryWrapper;
    }

    @Override
    public void reviewPicture(PictureReviewRequest pictureReviewRequest, User loginUser) {
        // 校验参数
        Integer reviewStatusValue = pictureReviewRequest.getReviewStatus();
        PictureReviewStatusEnum pictureReviewStatusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatusValue);
        ThrowUtils.throwIf(pictureReviewStatusEnum == null || pictureReviewStatusEnum == PictureReviewStatusEnum.REVIEWING,
                BusinessCodeEnum.PARAMS_ERROR, "审核状态错误");
        // 校验图片是否存在
        Picture picture = this.getById(pictureReviewRequest.getId());
        ThrowUtils.throwIf(picture == null, BusinessCodeEnum.NOT_FOUND_ERROR, "图片不存在");
        // 审核状态不应重复
        ThrowUtils.throwIf(picture.getReviewStatus() == pictureReviewStatusEnum.value, BusinessCodeEnum.PARAMS_ERROR, "重复审核");

        Picture updatePicture = new Picture();
        BeanUtil.copyProperties(pictureReviewRequest, updatePicture);
        updatePicture.setReviewerId(loginUser.getId());
        updatePicture.setReviewTime(new Date());

        // 更新库表
        boolean result = this.updateById(updatePicture);
        ThrowUtils.throwIf(!result, BusinessCodeEnum.OPERATION_ERROR, "图片审核失败");
    }

    @Override
    public void populateReviewParams(Picture picture, User loginUser) {
        if (userService.isAdmin(loginUser)) {
            // 管理员自动过审
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.value);
            picture.setReviewMessage("管理员自动过审");
            picture.setReviewerId(loginUser.getId());
            picture.setReviewTime(new Date());
        } else {
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.value);
        }
    }

    @Override
    public Integer fetchPicture(PictureFetchRequest pictureFetchRequest, User loginUser) {
        // 抓取图片地址
        String fetchUrl = String.format(FETCH_URL, pictureFetchRequest.getSearchText());
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

        String picNamePrefix = pictureFetchRequest.getPicNamePrefix();
        // 默认使用搜索词作为图片名称前缀
        if (StrUtil.isBlank(picNamePrefix)) {
            picNamePrefix = pictureFetchRequest.getSearchText();
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
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            pictureUploadRequest.setFileUrl(imgSrc);
            String picName = String.format("%s_%s_%s", picNamePrefix, now, uploadCount + 1);
            pictureUploadRequest.setPicName(picName);
            try {
                PictureVO pictureVO = this.uploadPicture(imgSrc, pictureUploadRequest, loginUser);
                uploadCount++;
                log.info("上传图片成功:{}", pictureVO);
            } catch (Exception e) {
                log.error("上传图片失败:{}", e.getMessage());
            }
            if (uploadCount >= pictureFetchRequest.getFetchSize()) {
                break;
            }
        }
        return uploadCount;
    }

    @Async
    @Override
    public void clearPictureFile(Picture picture) {
        String picUrl = picture.getPicUrl();
        // 判断图片文件是否被多条记录使用
        Long count = this.lambdaQuery()
                .eq(Picture::getPicUrl, picUrl)
                .count();
        if (count > 1) {
            return;
        }
        // 删除原图
        cosManager.deleteObject(picUrl);
        // 删除缩略图
        String thumbnailUrl = picture.getThumbnailUrl();
        if (StrUtil.isNotBlank(thumbnailUrl)) {
            cosManager.deleteObject(thumbnailUrl);
        }
    }

    @Override
    public void validatePictureAuth(Picture picture, User loginUser) {
        Long spaceId = picture.getSpaceId();
        Long userId = loginUser.getId();
        if (spaceId == null) {
            // 公共空间,图片创建人和管理员可操作
            ThrowUtils.throwIf(!userId.equals(picture.getUserId())
                    && !userService.isAdmin(loginUser), BusinessCodeEnum.NO_AUTH_ERROR);
        } else {
            // 私有空间,仅图片创建人可操作
            ThrowUtils.throwIf(!userId.equals(picture.getUserId()), BusinessCodeEnum.NO_AUTH_ERROR);
        }
    }

    @Override
    public Boolean deletePicture(Picture picture, User loginUser) {
        // 校验权限
        validatePictureAuth(picture, loginUser);

        transactionTemplate.execute(status -> {
            boolean result = removeById(picture.getId());
            ThrowUtils.throwIf(!result, BusinessCodeEnum.OPERATION_ERROR);

            if (picture.getSpaceId() != null) {
                // 更新空间已使用容量和数量
                LambdaUpdateWrapper<Space> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(Space::getId, picture.getSpaceId())
                        .setSql("current_size = current_size - " + picture.getPicSize())
                        .setSql("current_count = current_count - 1");
                int updated = spaceMapper.update(null, updateWrapper);
                ThrowUtils.throwIf(updated < 1, BusinessCodeEnum.SYSTEM_ERROR, "更新空间额度失败");
            }
            return true;
        });

        // 清理图片文件
        clearPictureFile(picture);
        return true;
    }

    @Override
    public Boolean updatePicture(PictureUpdateRequest pictureUpdateRequest, User loginUser) {
        // 判断图片是否存在
        long id = pictureUpdateRequest.getId();
        Picture oldPicture = getById(id);
        ThrowUtils.throwIf(oldPicture == null, BusinessCodeEnum.NOT_FOUND_ERROR);

        // 校验权限
        validatePictureAuth(oldPicture, loginUser);

        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUpdateRequest, picture);
        picture.setPicTags(JSONUtil.toJsonStr(pictureUpdateRequest.getPicTagList()));

        // 校验图片参数
        validatePicture(picture);

        // 填充审核参数
        populateReviewParams(picture, loginUser);

        // 更新库表
        boolean result = updateById(picture);
        ThrowUtils.throwIf(!result, BusinessCodeEnum.OPERATION_ERROR);
        return true;
    }

}