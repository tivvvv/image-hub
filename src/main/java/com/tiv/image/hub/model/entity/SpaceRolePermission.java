package com.tiv.image.hub.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 空间角色权限关联表
 *
 * @TableName space_role_permission
 */
@Data
@TableName(value = "space_role_permission")
public class SpaceRolePermission {

    /**
     * 关联id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 空间角色id
     */
    @TableField(value = "role_id")
    private Long roleId;

    /**
     * 空间权限id
     */
    @TableField(value = "permission_id")
    private Long permissionId;

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