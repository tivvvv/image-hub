package com.tiv.image.hub.model.dto.picture;

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
public class PictureUploadResult {

    /**
     * 图片名称
     */
    private String picName;

    /**
     * 图片url
     */
    private String picUrl;

    /**
     * 图片大小
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private int picWidth;

    /**
     * 图片高度
     */
    private int picHeight;

    /**
     * 图片宽高比
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

}