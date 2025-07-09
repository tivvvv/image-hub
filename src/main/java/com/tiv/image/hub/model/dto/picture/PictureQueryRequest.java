package com.tiv.image.hub.model.dto.picture;

import com.tiv.image.hub.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 图片查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PictureQueryRequest extends PageRequest implements Serializable {

    /**
     * 图片id
     */
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

    /**
     * 图片大小
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 图片宽高比
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 关键字
     */
    private String keyword;

    private static final long serialVersionUID = 1L;

}
