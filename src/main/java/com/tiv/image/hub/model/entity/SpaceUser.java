package com.tiv.image.hub.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 空间成员表
 *
 * @TableName space_user
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "space_user")
public class SpaceUser implements Serializable {

    /**
     * 空间成员关联id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 空间id
     */
    @TableField(value = "space_id")
    private Long spaceId;

    /**
     * 用户id
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 空间角色 viewer/editor/admin
     */
    @TableField(value = "space_role")
    private String spaceRole;

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