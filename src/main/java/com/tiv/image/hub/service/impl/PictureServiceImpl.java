package com.tiv.image.hub.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tiv.image.hub.common.BusinessCodeEnum;
import com.tiv.image.hub.manager.upload.FilePictureUpload;
import com.tiv.image.hub.mapper.PictureMapper;
import com.tiv.image.hub.model.dto.picture.PictureQueryRequest;
import com.tiv.image.hub.model.dto.picture.PictureReviewRequest;
import com.tiv.image.hub.model.dto.picture.PictureUploadRequest;
import com.tiv.image.hub.model.dto.picture.PictureUploadResult;
import com.tiv.image.hub.model.entity.Picture;
import com.tiv.image.hub.model.entity.User;
import com.tiv.image.hub.model.enums.PictureReviewStatusEnum;
import com.tiv.image.hub.model.enums.UserRoleEnum;
import com.tiv.image.hub.model.vo.PictureVO;
import com.tiv.image.hub.model.vo.UserVO;
import com.tiv.image.hub.service.PictureService;
import com.tiv.image.hub.service.UserService;
import com.tiv.image.hub.util.ThrowUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {

    @Autowired
    private FilePictureUpload filePictureUpload;

    @Resource
    private UserService userService;

    private static final int URL_MAX_LENGTH = 512;

    private static final int INTRO_MAX_LENGTH = 512;

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
    public PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) {
        Long pictureId = pictureUploadRequest.getId();
        if (pictureId != null) {
            // 更新图片,需校验图片是否存在
            Picture existedPicture = this.getById(pictureId);
            ThrowUtils.throwIf(existedPicture == null, BusinessCodeEnum.NOT_FOUND_ERROR, "图片不存在");
            // 仅创建人和管理员可更新图片
            ThrowUtils.throwIf(!loginUser.getId().equals(existedPicture.getUserId())
                    && !UserRoleEnum.ADMIN.value.equals(loginUser.getUserRole()), BusinessCodeEnum.NO_AUTH_ERROR);
        }

        // 按照用户id创建目录
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        // 上传图片
        PictureUploadResult pictureUploadResult = filePictureUpload.uploadPicture(multipartFile, uploadPathPrefix);

        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureUploadResult, picture);
        picture.setUserId(loginUser.getId());

        if (pictureId != null) {
            picture.setId(pictureId);
        }
        // 补充审核参数
        this.fillReviewParams(picture, loginUser);

        // 更新库表
        boolean result = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!result, BusinessCodeEnum.SYSTEM_ERROR, "保存图片失败");
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
    public void fillReviewParams(Picture picture, User loginUser) {
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

}