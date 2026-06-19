package com.tiv.image.hub.model.dto.space.user;

import lombok.Data;

import javax.validation.constraints.Min;
import java.io.Serializable;

/**
 * 空间成员更新请求
 */
@Data
public class SpaceUserUpdateRequest implements Serializable {

    /**
     * 空间成员关联id
     */
    @Min(value = 1L, message = "id不能小于1")
    private Long id;

    /**
     * 空间角色 viewer/editor/admin
     */
    private String spaceRole;

    private static final long serialVersionUID = 1L;

}