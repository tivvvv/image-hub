package com.tiv.image.hub.manager.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.tiv.image.hub.common.BusinessCodeEnum;
import com.tiv.image.hub.config.CosClientConfig;
import com.tiv.image.hub.exception.BusinessException;
import com.tiv.image.hub.manager.CosManager;
import com.tiv.image.hub.model.dto.picture.PictureUploadResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * 图片上传模版
 */
@Slf4j
@Component
public abstract class PictureUploadTemplate<T> {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    /**
     * 上传图片
     *
     * @param inputSource
     * @param uploadPathPrefix
     * @return
     */
    public PictureUploadResult uploadPicture(T inputSource, String uploadPathPrefix) {

        // 1. 校验图片
        validateFile(inputSource);

        // 2. 定义上传路径
        String uuid = RandomUtil.randomString(8);
        String originalFilename = getOriginalFilename(inputSource);
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);

        File file = null;
        try {
            file = File.createTempFile(uploadPath, null);
            // 3. 处理临时文件
            processFile(inputSource, file);
            // 4. 上传文件
            PutObjectResult putObjectResult = cosManager.putPicture(uploadPath, file);
            // 5. 构建返回结果
            return buildResult(putObjectResult, uploadPath, originalFilename, file);
        } catch (IOException e) {
            log.error("picture upload error, file path = {}", uploadPath, e);
            throw new BusinessException(BusinessCodeEnum.OPERATION_ERROR, "图片上传异常");
        } finally {
            // 6. 删除临时文件
            deleteTemporaryFile(file);
        }
    }

    /**
     * 校验文件
     *
     * @param inputSource
     */
    protected abstract void validateFile(T inputSource);

    /**
     * 获取原始文件名
     *
     * @param inputSource
     * @return
     */
    protected abstract String getOriginalFilename(T inputSource);

    /**
     * 处理临时文件
     *
     * @param inputSource
     * @param file
     */
    protected abstract void processFile(T inputSource, File file) throws IOException;

    /**
     * 构建返回结果
     *
     * @param putObjectResult
     * @param uploadPath
     * @param originalFilename
     * @param file
     * @return
     */
    private PictureUploadResult buildResult(PutObjectResult putObjectResult, String uploadPath, String originalFilename, File file) {
        List<CIObject> processedObjectList = putObjectResult.getCiUploadResult().getProcessResults().getObjectList();
        if (CollUtil.isNotEmpty(processedObjectList)) {
            // 压缩后的图片对象
            CIObject compressedCiObject = processedObjectList.get(0);
            int picWidth = compressedCiObject.getWidth();
            int picHeight = compressedCiObject.getHeight();
            // 计算图片宽高比
            double picScale = NumberUtil.round((double) picWidth / picHeight, 2).doubleValue();
            PictureUploadResult pictureUploadResult = PictureUploadResult
                    .builder()
                    .picUrl(cosClientConfig.getHost() + "/" + compressedCiObject.getKey())
                    .picName(FileUtil.mainName(originalFilename))
                    .picSize(compressedCiObject.getSize().longValue())
                    .picWidth(picWidth)
                    .picHeight(picHeight)
                    .picScale(picScale)
                    .picFormat(compressedCiObject.getFormat())
                    .build();
            // 设置缩略图url
            if (processedObjectList.size() > 1) {
                CIObject thumbnailCiObject = processedObjectList.get(1);
                pictureUploadResult.setThumbnailUrl(cosClientConfig.getHost() + "/" + thumbnailCiObject.getKey());
            }
            // 返回封装结果
            return pictureUploadResult;
        }

        // 获取原始图片信息对象
        ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
        int picWidth = imageInfo.getWidth();
        int picHeight = imageInfo.getHeight();
        // 计算图片宽高比
        double picScale = NumberUtil.round((double) picWidth / picHeight, 2).doubleValue();
        // 封装返回结果
        return PictureUploadResult
                .builder()
                .picUrl(cosClientConfig.getHost() + uploadPath)
                .picName(FileUtil.mainName(originalFilename))
                .picSize(FileUtil.size(file))
                .picWidth(picWidth)
                .picHeight(picHeight)
                .picScale(picScale)
                .picFormat(imageInfo.getFormat())
                .build();
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

}