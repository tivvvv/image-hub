package com.tiv.image.hub.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tiv.image.hub.annotation.AuthCheck;
import com.tiv.image.hub.common.BusinessCodeEnum;
import com.tiv.image.hub.common.BusinessResponse;
import com.tiv.image.hub.common.DeleteRequest;
import com.tiv.image.hub.constant.Constants;
import com.tiv.image.hub.model.dto.picture.PictureQueryRequest;
import com.tiv.image.hub.model.dto.picture.PictureUpdateRequest;
import com.tiv.image.hub.model.dto.picture.PictureUploadRequest;
import com.tiv.image.hub.model.entity.Picture;
import com.tiv.image.hub.model.entity.User;
import com.tiv.image.hub.model.enums.UserRoleEnum;
import com.tiv.image.hub.model.vo.PictureVO;
import com.tiv.image.hub.service.PictureService;
import com.tiv.image.hub.service.UserService;
import com.tiv.image.hub.util.ResultUtils;
import com.tiv.image.hub.util.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

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

    private static final int USER_QUERY_PICTURE_LIMIT = 20;

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

    /**
     * 更新图片
     *
     * @param pictureUpdateRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/update")
    public BusinessResponse<Boolean> updatePicture(@RequestBody @Valid PictureUpdateRequest pictureUpdateRequest, HttpServletRequest httpServletRequest) {

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
        // 获取封装类
        return ResultUtils.success(pictureService.getPictureVO(picture));
    }

    /**
     * 分页获取图片视图列表
     */
    @PostMapping("/page/vo")
    public BusinessResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        ThrowUtils.throwIf(pictureQueryRequest.getPageSize() > USER_QUERY_PICTURE_LIMIT, BusinessCodeEnum.PARAMS_ERROR);

        Page<Picture> picturePage = doListPicture(pictureQueryRequest);
        // 获取封装类
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage));
    }

    /**
     * 删除图片
     *
     * @param deleteRequest
     * @param httpServletRequest
     * @return
     */
    @DeleteMapping()
    public BusinessResponse<Boolean> deletePicture(@RequestBody @Valid DeleteRequest deleteRequest, HttpServletRequest httpServletRequest) {
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
