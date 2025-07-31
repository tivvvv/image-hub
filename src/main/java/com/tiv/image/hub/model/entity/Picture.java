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
 * @TableName picture
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "picture")
public class Picture implements Serializable {

    /**
     * 图片id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 图片名称
     */
    @TableField(value = "pic_name")
    private String picName;

    /**
     * 图片简介
     */
    @TableField(value = "pic_intro")
    private String picIntro;

    /**
     * 图片url
     */
    @TableField(value = "pic_url")
    private String picUrl;

    /**
     * 图片分类
     */
    @TableField(value = "pic_category")
    private String picCategory;

    /**
     * 图片标签(JSON)
     */
    @TableField(value = "pic_tags")
    private String picTags;

    /**
     * 图片大小
     */
    @TableField(value = "pic_size")
    private Long picSize;

    /**
     * 图片宽度
     */
    @TableField(value = "pic_width")
    private Integer picWidth;

    /**
     * 图片高度
     */
    @TableField(value = "pic_height")
    private Integer picHeight;

    /**
     * 图片宽高比
     */
    @TableField(value = "pic_scale")
    private Double picScale;

    /**
     * 图片格式
     */
    @TableField(value = "pic_format")
    private String picFormat;

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