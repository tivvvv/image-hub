package com.tiv.image.hub.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tiv.image.hub.model.dto.picture.PictureQueryRequest;
import com.tiv.image.hub.model.dto.picture.PictureUploadRequest;
import com.tiv.image.hub.model.entity.Picture;
import com.tiv.image.hub.model.entity.User;
import com.tiv.image.hub.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

public interface PictureService extends IService<Picture> {

    /**
     * 校验图片
     *
     * @param picture
     */
    void validatePicture(Picture picture);

    /**
     * 上传图片
     *
     * @param multipartFile
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser);

    /**
     * 获取图片视图(脱敏)
     *
     * @param picture
     * @return
     */
    PictureVO getPictureVO(Picture picture);

    /**
     * 分页获取图片视图(脱敏)
     *
     * @param picturePage
     * @return
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage);

    /**
     * 获取图片查询条件
     *
     * @param pictureQueryRequest
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

}