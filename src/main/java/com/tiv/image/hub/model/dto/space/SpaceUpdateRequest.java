package com.tiv.image.hub.model.dto.space;

import lombok.Data;

import javax.validation.constraints.Min;
import java.io.Serializable;

/**
 * 空间更新请求
 */
@Data
public class SpaceUpdateRequest implements Serializable {

    /**
     * 空间id
     */
    @Min(value = 1L, message = "id不能小于1")
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别 0:普通版,1:专业版,2:旗舰版
     */
    private Integer spaceLevel;

    /**
     * 空间图片的最大容量
     */
    private Long maxSize;

    /**
     * 空间图片的最大数量
     */
    private Long maxCount;

    private static final long serialVersionUID = 1L;

}
