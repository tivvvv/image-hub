package com.tiv.image.hub.model.dto.image.request;

import com.tiv.image.hub.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 图片查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ImageQueryRequest extends PageRequest implements Serializable {

    /**
     * 图片id
     */
    private Long id;

    /**
     * 图片名称
     */
    private String imageName;

    /**
     * 图片简介
     */
    private String imageIntro;

    /**
     * 图片分类
     */
    private String imageCategory;

    /**
     * 图片标签
     */
    private List<String> imageTagList;

    /**
     * 图片大小
     */
    private Long imageSize;

    /**
     * 图片宽度
     */
    private Integer imageWidth;

    /**
     * 图片高度
     */
    private Integer imageHeight;

    /**
     * 图片宽高比
     */
    private Double imageScale;

    /**
     * 图片格式
     */
    private String imageFormat;

    /**
     * 图片主色调
     */
    private String imageColor;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 空间id(null为公共空间)
     */
    private Long spaceId;

    /**
     * 关键字
     */
    private String keyword;

    /**
     * 审核状态 0:审核中,1:通过,2:驳回
     */
    private Integer reviewStatus;

    /**
     * 审核人id
     */
    private Long reviewerId;

    /**
     * 审核时间
     */
    private Date reviewTime;

    /**
     * 查询时间段开始时间
     */
    private Date updateTimeStart;

    /**
     * 查询时间段结束时间
     */
    private Date updateTimeEnd;

    private static final long serialVersionUID = 1L;

}
