package com.tiv.image.hub.model.dto.picture;

import lombok.Data;

import javax.validation.constraints.Min;
import java.io.Serializable;
import java.util.List;

/**
 * 图片更新请求
 */
@Data
public class PictureUpdateRequest implements Serializable {

    /**
     * 图片id
     */
    @Min(value = 1L, message = "id不能小于1")
    private Long id;

    /**
     * 图片名称
     */
    private String picName;

    /**
     * 图片简介
     */
    private String picIntro;

    /**
     * 图片分类
     */
    private String picCategory;

    /**
     * 图片标签
     */
    private List<String> picTagList;

    private static final long serialVersionUID = 1L;

}
