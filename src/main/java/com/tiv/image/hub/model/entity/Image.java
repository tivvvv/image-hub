package com.tiv.image.hub.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 图片表
 *
 * @TableName image
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "image")
public class Image implements Serializable {

    /**
     * 图片id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 图片名称
     */
    @TableField(value = "image_name")
    private String imageName;

    /**
     * 图片简介
     */
    @TableField(value = "image_intro")
    private String imageIntro;

    /**
     * 图片url
     */
    @TableField(value = "image_url")
    private String imageUrl;

    /**
     * 图片分类
     */
    @TableField(value = "image_category")
    private String imageCategory;

    /**
     * 图片标签(JSON)
     */
    @TableField(value = "image_tags")
    private String imageTags;

    /**
     * 图片大小
     */
    @TableField(value = "image_size")
    private Long imageSize;

    /**
     * 图片宽度
     */
    @TableField(value = "image_width")
    private Integer imageWidth;

    /**
     * 图片高度
     */
    @TableField(value = "image_height")
    private Integer imageHeight;

    /**
     * 图片宽高比
     */
    @TableField(value = "image_scale")
    private Double imageScale;

    /**
     * 图片格式
     */
    @TableField(value = "image_format")
    private String imageFormat;

    /**
     * 图片主色调 格式:0xRRGGBB
     */
    @TableField(value = "image_color")
    private String imageColor;

    /**
     * 缩略图url
     */
    @TableField(value = "thumbnail_url")
    private String thumbnailUrl;

    /**
     * 创建用户id
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 空间id(null为公共空间)
     */
    @TableField(value = "space_id")
    private Long spaceId;

    /**
     * 审核状态 0:审核中,1:通过,2:驳回
     */
    @TableField(value = "review_status")
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    @TableField(value = "review_message")
    private String reviewMessage;

    /**
     * 审核人id
     */
    @TableField(value = "reviewer_id")
    private Long reviewerId;

    /**
     * 审核时间
     */
    @TableField(value = "review_time")
    private Date reviewTime;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    @TableField(value = "deleted")
    private Integer deleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}