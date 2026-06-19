package com.tiv.image.hub.model.dto.space.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间成员查询请求
 */
@Data
public class SpaceUserQueryRequest implements Serializable {

    /**
     * 空间成员关联id
     */
    private Long id;

    /**
     * 空间id
     */
    private Long spaceId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 空间角色 viewer/editor/admin
     */
    private String spaceRole;

    private static final long serialVersionUID = 1L;

}