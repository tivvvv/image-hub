package com.tiv.image.hub.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 空间表
 *
 * @TableName space
 */
@Data
@TableName(value = "space")
public class Space {

    /**
     * 空间id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 空间名称
     */
    @TableField(value = "space_name")
    private String spaceName;

    /**
     * 空间级别 0:普通版,1:专业版,2:旗舰版
     */
    @TableField(value = "space_level")
    private Integer spaceLevel;

    /**
     * 空间图片的最大容量
     */
    @TableField(value = "max_size")
    private Long maxSize;

    /**
     * 空间图片的最大数量
     */
    @TableField(value = "max_count")
    private Long maxCount;

    /**
     * 当前空间已使用容量
     */
    @TableField(value = "current_size")
    private Long currentSize;

    /**
     * 当前空间已使用数量
     */
    @TableField(value = "current_count")
    private Long currentCount;

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