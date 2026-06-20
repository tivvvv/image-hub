package com.tiv.image.hub.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 空间权限表
 *
 * @TableName space_permission
 */
@Data
@TableName(value = "space_permission")
public class SpacePermission {

    /**
     * 空间权限id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 权限标识
     */
    @TableField(value = "permission_key")
    private String permissionKey;

    /**
     * 权限名称
     */
    @TableField(value = "permission_name")
    private String permissionName;

    /**
     * 资源
     */
    @TableField(value = "resource")
    private String resource;

    /**
     * 操作
     */
    @TableField(value = "action")
    private String action;

    /**
     * 权限描述
     */
    @TableField(value = "permission_desc")
    private String permissionDesc;

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