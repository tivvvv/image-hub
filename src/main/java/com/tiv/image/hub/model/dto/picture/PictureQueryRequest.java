package com.tiv.image.hub.model.dto.picture;

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
