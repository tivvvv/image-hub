package com.tiv.image.hub.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.tiv.image.hub.common.BusinessCodeEnum;
import com.tiv.image.hub.config.CosClientConfig;
import com.tiv.image.hub.constant.Constants;
import com.tiv.image.hub.exception.BusinessException;
import com.tiv.image.hub.model.dto.picture.PictureUploadResult;
import com.tiv.image.hub.util.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

/**
 * 文件服务
 */
@Slf4j
@Component
public class PictureManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    /**
     * 上传图片
     *
     * @param multipartFile
     * @param uploadPathPrefix
     * @return
     */
    public PictureUploadResult uploadPicture(MultipartFile multipartFile, String uploadPathPrefix) {

        // 1. 校验图片
        validateFile(multipartFile);

        // 2. 定义上传路径
        String uuid = RandomUtil.randomString(8);
        String originalFilename = multipartFile.getOriginalFilename();
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);
        // 3. 上传图片
        File file = null;
        try {
            // MultipartFile转File
            file = File.createTempFile(uploadPath, null);
            multipartFile.transferTo(file);
            // 上传文件
            PutObjectResult putObjectResult = cosManager.putPicture(uploadPath, file);
            // 获取图片信息对象
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            int picWidth = imageInfo.getWidth();
            int picHeight = imageInfo.getHeight();
            // 计算图片宽高比
            double picScale = NumberUtil.round((double) picWidth / picHeight, 2).doubleValue();
            // 封装返回结果
            PictureUploadResult pictureUploadResult = PictureUploadResult.builder()
                    .picUrl(cosClientConfig.getHost() + uploadPath)
                    .picName(FileUtil.mainName(originalFilename))
                    .picSize(FileUtil.size(file))
                    .picWidth(picWidth)
                    .picHeight(picHeight)
                    .picScale(picScale)
                    .picFormat(imageInfo.getFormat()).build();
            return pictureUploadResult;
        } catch (IOException e) {
            log.error("picture upload error, file path = {}", uploadPath, e);
            throw new BusinessException(BusinessCodeEnum.OPERATION_ERROR, "图片上传异常");
        } finally {
            // 删除临时文件
            deleteTemporaryFile(file);
        }
    }

    /**
     * 根据url上传图片
     *
     * @param fileUrl
     * @param uploadPathPrefix
     * @return
     */
    public PictureUploadResult uploadPictureByUrl(String fileUrl, String uploadPathPrefix) {

        // 1. 校验图片
        validateFile(fileUrl);

        // 2. 定义上传路径
        String uuid = RandomUtil.randomString(8);
        String originalFilename = FileUtil.mainName(fileUrl);
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);
        // 3. 上传图片
        File file = null;
        try {
            // MultipartFile转File
            file = File.createTempFile(uploadPath, null);
            // 下载文件
            HttpUtil.downloadFile(fileUrl, file);
            // 上传文件
            PutObjectResult putObjectResult = cosManager.putPicture(uploadPath, file);
            // 获取图片信息对象
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            int picWidth = imageInfo.getWidth();
            int picHeight = imageInfo.getHeight();
            // 计算图片宽高比
            double picScale = NumberUtil.round((double) picWidth / picHeight, 2).doubleValue();
            // 封装返回结果
            PictureUploadResult pictureUploadResult = PictureUploadResult.builder()
                    .picUrl(cosClientConfig.getHost() + uploadPath)
                    .picName(FileUtil.mainName(originalFilename))
                    .picSize(FileUtil.size(file))
                    .picWidth(picWidth)
                    .picHeight(picHeight)
                    .picScale(picScale)
                    .picFormat(imageInfo.getFormat()).build();
            return pictureUploadResult;
        } catch (IOException e) {
            log.error("picture upload error, file path = {}", uploadPath, e);
            throw new BusinessException(BusinessCodeEnum.OPERATION_ERROR, "图片上传异常");
        } finally {
            // 删除临时文件
            deleteTemporaryFile(file);
        }
    }

    /**
     * 删除临时文件
     *
     * @param file
     */
    private void deleteTemporaryFile(File file) {
        if (file == null) {
            return;
        }
        boolean deleted = file.delete();
        if (!deleted) {
            log.error("temporary file delete error, file path = {}", file.getAbsoluteFile());
        }
    }

    /**
     * 校验文件
     *
     * @param multipartFile
     */
    private void validateFile(MultipartFile multipartFile) {

        // 1. 校验文件大小
        ThrowUtils.throwIf(multipartFile == null, BusinessCodeEnum.PARAMS_ERROR, "文件不能为null");
        long size = multipartFile.getSize();
        ThrowUtils.throwIf(size > 10 * Constants.ONE_MEGA_BYTES, BusinessCodeEnum.PARAMS_ERROR, "文件不能超过10M");

        // 2. 校验文件后缀
        String suffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        ThrowUtils.throwIf(!Constants.VALID_IMAGE_SUFFIXES.contains(suffix), BusinessCodeEnum.PARAMS_ERROR, "文件格式不支持");
    }

    /**
     * 根据url校验文件
     *
     * @param fileUrl
     */
    private void validateFile(String fileUrl) {
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

}