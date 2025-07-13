package com.tiv.image.hub.model.vo;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.tiv.image.hub.model.entity.Picture;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 图片视图
 */
@Data
public class PictureVO implements Serializable {

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
     * 图片url
     */
    private String picUrl;

    /**
     * 图片分类
     */
    private String picCategory;

    /**
     * 图片标签列表
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
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建用户信息
     */
    private UserVO userVO;

    private static final long serialVersionUID = 1L;

    /**
     * 封装类转实体类
     *
     * @param pictureVO
     * @return
     */
    public static Picture transferToObj(PictureVO pictureVO) {
        if (pictureVO == null) {
            return null;
        }
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureVO, picture);
        // 类型不同的字段需要额外处理
        picture.setPicTags(JSONUtil.toJsonStr(pictureVO.getPicTagList()));
        return picture;
    }

    /**
     * 实体类转封装类
     *
     * @param picture
     * @return
     */
    public static PictureVO transferToVO(Picture picture) {
        if (picture == null) {
            return null;
        }
        PictureVO pictureVO = new PictureVO();
        BeanUtil.copyProperties(picture, pictureVO);
        // 类型不同的字段需要额外处理
        pictureVO.setPicTagList(JSONUtil.toList(picture.getPicTags(), String.class));
        return pictureVO;
    }

}
