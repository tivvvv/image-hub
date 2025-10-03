package com.tiv.image.hub.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tiv.image.hub.model.dto.picture.*;
import com.tiv.image.hub.model.entity.Picture;
import com.tiv.image.hub.model.entity.User;
import com.tiv.image.hub.model.vo.PictureVO;

public interface PictureService extends IService<Picture> {

    /**
     * 校验图片参数
     *
     * @param picture
     */
    void validatePicture(Picture picture);

    /**
     * 上传图片
     *
     * @param inputSource
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser);

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

    /**
     * 审核图片
     *
     * @param pictureReviewRequest
     * @param loginUser
     */
    void reviewPicture(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 填充审核参数
     *
     * @param picture
     * @param loginUser
     */
    void populateReviewParams(Picture picture, User loginUser);

    /**
     * 抓取图片
     *
     * @param pictureFetchRequest
     * @param loginUser
     * @return
     */
    Integer fetchPicture(PictureFetchRequest pictureFetchRequest, User loginUser);

    /**
     * 异步清理图片文件
     *
     * @param picture
     */
    void clearPictureFile(Picture picture);

    /**
     * 校验用户是否有指定图片权限
     *
     * @param picture
     * @param loginUser
     */
    void validatePictureAuth(Picture picture, User loginUser);

    /**
     * 删除图片
     *
     * @param picture
     * @param loginUser
     * @return
     */
    Boolean deletePicture(Picture picture, User loginUser);

    /**
     * 更新图片
     *
     * @param pictureUpdateRequest
     * @param loginUser
     * @return
     */
    Boolean updatePicture(PictureUpdateRequest pictureUpdateRequest, User loginUser);

}