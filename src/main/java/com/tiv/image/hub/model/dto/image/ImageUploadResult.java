package com.tiv.image.hub.model.dto.image;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图片上传结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadResult {

    /**
     * 图片名称
     */
    private String imageName;

    /**
     * 图片url
     */
    private String imageUrl;

    /**
     * 图片大小
     */
    private Long imageSize;

    /**
     * 图片宽度
     */
    private int imageWidth;

    /**
     * 图片高度
     */
    private int imageHeight;

    /**
     * 图片宽高比
     */
    private Double imageScale;

    /**
     * 图片格式
     */
    private String imageFormat;

    /**
     * 缩略图url
     */
    private String thumbnailUrl;

}