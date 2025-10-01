package com.tiv.image.hub.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tiv.image.hub.model.dto.space.SpaceAddRequest;
import com.tiv.image.hub.model.dto.space.SpaceQueryRequest;
import com.tiv.image.hub.model.entity.Space;
import com.tiv.image.hub.model.entity.User;
import com.tiv.image.hub.model.vo.SpaceVO;

public interface SpaceService extends IService<Space> {

    /**
     * 校验空间参数参数
     *
     * @param space
     * @param isAdd
     */
    void validateSpace(Space space, boolean isAdd);

    /**
     * 创建空间
     *
     * @param spaceAddRequest
     * @param loginUser
     * @return
     */
    SpaceVO addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    /**
     * 获取空间视图(脱敏)
     *
     * @param space
     * @return
     */
    SpaceVO getSpaceVO(Space space);

    /**
     * 分页获取空间视图(脱敏)
     *
     * @param spacePage
     * @return
     */
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage);

    /**
     * 获取空间查询条件
     *
     * @param spaceQueryRequest
     * @return
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 根据空间级别填充空间配额
     *
     * @param space
     */
    void populateQuotaBySpaceLevel(Space space);

}
