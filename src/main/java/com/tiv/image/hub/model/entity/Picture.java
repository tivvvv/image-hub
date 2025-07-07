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
    @TableId(value = "id", type = IdType.AUTO)
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
     * 创建用户id
     */
    @TableField(value = "user_id")
    private Long userId;

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