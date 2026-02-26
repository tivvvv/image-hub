package com.tiv.image.hub.model.vo;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.tiv.image.hub.model.entity.Image;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 图片视图
 */
@Data
public class ImageVO implements Serializable {

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
     * 图片url
     */
    private String imageUrl;

    /**
     * 图片分类
     */
    private String imageCategory;

    /**
     * 图片标签列表
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
     * 缩略图url
     */
    private String thumbnailUrl;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 空间id(null为公共空间)
     */
    private Long spaceId;

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
     * @param imageVO
     * @return
     */
    public static Image transferToObj(ImageVO imageVO) {
        if (imageVO == null) {
            return null;
        }
        Image image = new Image();
        BeanUtil.copyProperties(imageVO, image);
        // 类型不同的字段需要额外处理
        image.setImageTags(JSONUtil.toJsonStr(imageVO.getImageTagList()));
        return image;
    }

    /**
     * 实体类转封装类
     *
     * @param image
     * @return
     */
    public static ImageVO transferToVO(Image image) {
        if (image == null) {
            return null;
        }
        ImageVO imageVO = new ImageVO();
        BeanUtil.copyProperties(image, imageVO);
        // 类型不同的字段需要额外处理
        imageVO.setImageTagList(JSONUtil.toList(image.getImageTags(), String.class));
        return imageVO;
    }

}
