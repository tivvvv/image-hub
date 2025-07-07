package com.tiv.image.hub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tiv.image.hub.model.dto.picture.PictureUploadRequest;
import com.tiv.image.hub.model.entity.Picture;
import com.tiv.image.hub.model.entity.User;
import com.tiv.image.hub.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     *
     * @param multipartFile
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser);

}