package com.tiv.image.hub.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tiv.image.hub.model.dto.image.request.*;
import com.tiv.image.hub.model.entity.Image;
import com.tiv.image.hub.model.entity.User;
import com.tiv.image.hub.model.vo.ImageVO;

public interface ImageService extends IService<Image> {

    /**
     * 校验图片参数
     *
     * @param image
     */
    void validateImage(Image image);

    /**
     * 上传图片
     *
     * @param inputSource
     * @param imageUploadRequest
     * @param loginUser
     * @return
     */
    ImageVO uploadImage(Object inputSource, ImageUploadRequest imageUploadRequest, User loginUser);

    /**
     * 获取图片视图(脱敏)
     *
     * @param image
     * @return
     */
    ImageVO getImageVO(Image image);

    /**
     * 分页获取图片视图(脱敏)
     *
     * @param imagePage
     * @return
     */
    Page<ImageVO> getImageVOPage(Page<Image> imagePage);

    /**
     * 获取图片查询条件
     *
     * @param imageQueryRequest
     * @return
     */
    QueryWrapper<Image> getQueryWrapper(ImageQueryRequest imageQueryRequest);

    /**
     * 审核图片
     *
     * @param imageReviewRequest
     * @param loginUser
     */
    void reviewImage(ImageReviewRequest imageReviewRequest, User loginUser);

    /**
     * 填充审核参数
     *
     * @param image
     * @param loginUser
     */
    void populateReviewParams(Image image, User loginUser);

    /**
     * 抓取图片
     *
     * @param imageFetchRequest
     * @param loginUser
     * @return
     */
    Integer fetchImage(ImageFetchRequest imageFetchRequest, User loginUser);

    /**
     * 异步清理图片文件
     *
     * @param image
     */
    void clearImageFile(Image image);

    /**
     * 校验用户是否有指定图片权限
     *
     * @param image
     * @param loginUser
     */
    void validateImageAuth(Image image, User loginUser);

    /**
     * 删除图片
     *
     * @param image
     * @param loginUser
     * @return
     */
    Boolean deleteImage(Image image, User loginUser);

    /**
     * 更新图片
     *
     * @param imageUpdateRequest
     * @param loginUser
     * @return
     */
    Boolean updateImage(ImageUpdateRequest imageUpdateRequest, User loginUser);

}