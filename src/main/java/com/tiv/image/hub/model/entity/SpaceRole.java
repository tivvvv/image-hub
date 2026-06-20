package com.tiv.image.hub.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 空间角色表
 *
 * @TableName space_role
 */
@Data
@TableName(value = "space_role")
public class SpaceRole {

    /**
     * 空间角色id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 角色标识 viewer/editor/admin
     */
    @TableField(value = "role_key")
    private String roleKey;

    /**
     * 角色名称
     */
    @TableField(value = "role_name")
    private String roleName;

    /**
     * 角色描述
     */
    @TableField(value = "role_desc")
    private String roleDesc;

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
    @TableField(value = "deleted")
    private Integer deleted;

}