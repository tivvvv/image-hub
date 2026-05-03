package com.tiv.image.hub.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 图片标签/分类列表视图
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageTagCategoryVO {

    /**
     * 标签列表
     */
    private List<String> tagList;

    /**
     * 分类列表
     */
    private List<String> categoryList;

}
