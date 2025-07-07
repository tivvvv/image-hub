package com.tiv.image.hub.controller;

import com.tiv.image.hub.annotation.AuthCheck;
import com.tiv.image.hub.common.BusinessResponse;
import com.tiv.image.hub.model.dto.picture.PictureUploadRequest;
import com.tiv.image.hub.model.entity.User;
import com.tiv.image.hub.model.vo.PictureVO;
import com.tiv.image.hub.service.PictureService;
import com.tiv.image.hub.service.UserService;
import com.tiv.image.hub.util.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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

    /**
     * 上传图片
     *
     * @param multipartFile
     * @param pictureUploadRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/upload")
    @AuthCheck(mustRole = "admin")
    public BusinessResponse<PictureVO> uploadPicture(@RequestPart("file") MultipartFile multipartFile,
                                                     PictureUploadRequest pictureUploadRequest,
                                                     HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser));
    }

}
