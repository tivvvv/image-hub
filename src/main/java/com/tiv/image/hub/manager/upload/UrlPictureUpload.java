package com.tiv.image.hub.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.tiv.image.hub.common.BusinessCodeEnum;
import com.tiv.image.hub.constant.Constants;
import com.tiv.image.hub.exception.BusinessException;
import com.tiv.image.hub.util.ThrowUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * url图片上传
 */
@Service
public class UrlPictureUpload extends PictureUploadTemplate<String> {

    @Override
    protected void validateFile(String fileUrl) {
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), BusinessCodeEnum.PARAMS_ERROR, "文件url不能为空");

        // 校验url格式
        try {
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(BusinessCodeEnum.PARAMS_ERROR, "文件url格式错误");
        }

        // 校验url协议
        ThrowUtils.throwIf(!StrUtil.startWith(fileUrl, "http://") && !StrUtil.startWith(fileUrl, "https://"),
                BusinessCodeEnum.PARAMS_ERROR, "文件url协议不支持");

        // head请求校验文件是否存在
        try (HttpResponse httpResponse = HttpUtil.createRequest(Method.HEAD, fileUrl).execute()) {
            // 响应异常,可能是不支持head请求,正常返回
            if (httpResponse.getStatus() != HttpStatus.HTTP_OK) {
                return;
            }
            // 校验文件类型
            String contentType = httpResponse.header("Content-Type");
            if (StrUtil.isNotBlank(contentType)) {
                ThrowUtils.throwIf(!Constants.VALID_CONTENT_TYPE.contains(contentType.toLowerCase()), BusinessCodeEnum.PARAMS_ERROR, "文件类型不支持");
            }
            // 校验文件大小
            String contentLength = httpResponse.header("Content-Length");
            if (StrUtil.isNotBlank(contentLength)) {
                long size = Long.parseLong(contentLength);
                ThrowUtils.throwIf(size > 10 * Constants.ONE_MEGA_BYTES, BusinessCodeEnum.PARAMS_ERROR, "文件不能超过10M");
            }
        } catch (Exception e) {
            throw new BusinessException(BusinessCodeEnum.PARAMS_ERROR, "文件url校验异常");
        }
    }

    @Override
    protected String getOriginalFilename(String fileUrl) {
        return FileUtil.mainName(fileUrl);
    }

    @Override
    protected void processFile(String fileUrl, File file) {
        // 下载文件
        HttpUtil.downloadFile(fileUrl, file);
    }

}
