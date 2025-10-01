package com.tiv.image.hub.model.vo;

import cn.hutool.core.bean.BeanUtil;
import com.tiv.image.hub.model.entity.Space;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 空间视图
 */
@Data
public class SpaceVO implements Serializable {

    /**
     * 空间id
     */
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

    /**
     * 当前空间已使用容量
     */
    private Long currentSize;

    /**
     * 当前空间已使用数量
     */
    private Long currentCount;

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
     * @param spaceVO
     * @return
     */
    public static Space transferToObj(SpaceVO spaceVO) {
        if (spaceVO == null) {
            return null;
        }
        Space space = new Space();
        BeanUtil.copyProperties(spaceVO, space);
        return space;
    }

    /**
     * 实体类转封装类
     *
     * @param space
     * @return
     */
    public static SpaceVO transferToVO(Space space) {
        if (space == null) {
            return null;
        }
        SpaceVO spaceVO = new SpaceVO();
        BeanUtil.copyProperties(space, spaceVO);
        return spaceVO;
    }

}
