package com.tiv.image.hub.manager.upload;

import cn.hutool.core.io.FileUtil;
import com.tiv.image.hub.common.BusinessCodeEnum;
import com.tiv.image.hub.constant.Constants;
import com.tiv.image.hub.util.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * 文件图片上传
 */
@Service
public class FilePictureUpload extends PictureUploadTemplate<MultipartFile> {

    @Override
    protected void validateFile(MultipartFile multipartFile) {

        // 1. 校验文件大小
        ThrowUtils.throwIf(multipartFile == null, BusinessCodeEnum.PARAMS_ERROR, "文件不能为null");
        long size = multipartFile.getSize();
        ThrowUtils.throwIf(size > 10 * Constants.ONE_MEGA_BYTES, BusinessCodeEnum.PARAMS_ERROR, "文件不能超过10M");

        // 2. 校验文件后缀
        String suffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        ThrowUtils.throwIf(!Constants.VALID_IMAGE_SUFFIXES.contains(suffix), BusinessCodeEnum.PARAMS_ERROR, "文件格式不支持");
    }

    @Override
    protected String getOriginalFilename(MultipartFile multipartFile) {
        return multipartFile.getOriginalFilename();
    }

    @Override
    protected void processFile(MultipartFile multipartFile, File file) throws IOException {
        multipartFile.transferTo(file);
    }

}
