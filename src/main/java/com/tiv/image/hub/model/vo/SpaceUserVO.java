package com.tiv.image.hub.model.vo;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.tiv.image.hub.model.entity.SpaceUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 空间成员视图
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpaceUserVO implements Serializable {

    /**
     * 空间成员关联id
     */
    private Long id;

    /**
     * 空间id
     */
    private Long spaceId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 空间角色 viewer/editor/admin
     */
    private String spaceRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 用户视图
     */
    private UserVO userVO;

    /**
     * 空间视图
     */
    private SpaceVO spaceVO;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 实体类转封装类
     *
     * @param spaceUser
     * @return
     */
    public static SpaceUserVO transferToVO(SpaceUser spaceUser) {
        if (spaceUser == null) {
            return null;
        }
        SpaceUserVO spaceUserVO = new SpaceUserVO();
        BeanUtil.copyProperties(spaceUser, spaceUserVO);
        return spaceUserVO;
    }

}