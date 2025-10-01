package com.tiv.image.hub.model.dto.space;

import com.tiv.image.hub.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 空间查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SpaceQueryRequest extends PageRequest implements Serializable {

    /**
     * 空间id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 空间级别 0:普通版,1:专业版,2:旗舰版
     */
    private Integer spaceLevel;

    private static final long serialVersionUID = 1L;

}
