package com.tiv.image.hub.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tiv.image.hub.common.BusinessCodeEnum;
import com.tiv.image.hub.manager.PictureManager;
import com.tiv.image.hub.mapper.PictureMapper;
import com.tiv.image.hub.model.dto.picture.PictureUploadRequest;
import com.tiv.image.hub.model.dto.picture.PictureUploadResult;
import com.tiv.image.hub.model.entity.Picture;
import com.tiv.image.hub.model.entity.User;
import com.tiv.image.hub.model.vo.PictureVO;
import com.tiv.image.hub.service.PictureService;
import com.tiv.image.hub.util.ThrowUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {

    @Resource
    private PictureMapper pictureMapper;
    @Autowired
    private PictureManager pictureManager;

    @Override
    public PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) {
        Long pictureId = pictureUploadRequest.getId();
        if (pictureId != null) {
            // 更新图片,需校验图片是否存在
            ThrowUtils.throwIf(!this.lambdaQuery().eq(Picture::getId, pictureId).exists(), BusinessCodeEnum.NOT_FOUND_ERROR, "图片不存在");
        }

        // 按照用户id创建目录
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        // 上传图片
        PictureUploadResult pictureUploadResult = pictureManager.uploadPicture(multipartFile, uploadPathPrefix);

        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureUploadResult, picture);
        picture.setUserId(loginUser.getId());
        // 更新库表
        if (pictureId != null) {
            picture.setId(pictureId);
        }
        boolean result = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!result, BusinessCodeEnum.SYSTEM_ERROR, "保存图片失败");
        return PictureVO.transferToVO(picture);
    }

}