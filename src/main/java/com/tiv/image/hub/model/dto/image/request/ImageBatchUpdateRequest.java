package com.tiv.image.hub.model.dto.image.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 图片批量更新请求
 */
@Data
public class ImageBatchUpdateRequest implements Serializable {

    /**
     * 图片id列表
     */
    @NotEmpty(message = "图片id列表不能为空")
    private List<Long> imageIds;

    /**
     * 空间id
     */
    @NotNull(message = "空间id不能为空")
    private Long spaceId;

    /**
     * 图片分类
     */
    private String imageCategory;

    /**
     * 图片标签
     */
    private List<String> imageTagList;

    private static final long serialVersionUID = 1L;

}