package com.tiv.image.hub.manager;

import cn.hutool.core.io.FileUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.tiv.image.hub.config.CosClientConfig;
import com.tiv.image.hub.constant.Constants;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 腾讯云对象存储服务
 */
@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    private static final String WEBP = ".webp";

    private static final String THUMBNAIL = "_thumbnail.";

    /**
     * 上传对象
     *
     * @param key
     * @param file
     * @return
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 获取对象
     *
     * @param key
     * @return
     */
    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }

    /**
     * 上传图片
     *
     * @param key
     * @param file
     * @return
     */
    public PutObjectResult putPicture(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        // 处理图片
        PicOperations picOperations = new PicOperations();
        // 1表示返回原图信息
        picOperations.setIsPicInfo(1);

        // 对大于1MB对图片进行压缩并生成缩略图
        if (Constants.cosProcessFlag && file.length() >= 1024 * 1024) {
            List<PicOperations.Rule> rules = new ArrayList<>();
            // 压缩图片
            String webpKey = FileUtil.mainName(key) + WEBP;
            PicOperations.Rule compressRule = new PicOperations.Rule();
            compressRule.setFileId(webpKey);
            compressRule.setRule("imageMogr2/format/webp");
            compressRule.setBucket(cosClientConfig.getBucket());
            rules.add(compressRule);

            // 处理缩略图
            String thumbnailKey = FileUtil.mainName(key) + THUMBNAIL + FileUtil.getSuffix(key);
            PicOperations.Rule thumbnailRule = new PicOperations.Rule();
            thumbnailRule.setFileId(thumbnailKey);
            thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>", 512, 512));
            thumbnailRule.setBucket(cosClientConfig.getBucket());
            rules.add(thumbnailRule);

            picOperations.setRules(rules);
        }

        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 删除对象
     *
     * @param key
     */
    public void deleteObject(String key) {
        cosClient.deleteObject(cosClientConfig.getBucket(), key);
    }

}
