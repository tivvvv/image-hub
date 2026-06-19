package com.tiv.image.hub.controller;

import cn.hutool.core.util.ObjectUtil;
import com.tiv.image.hub.common.BusinessCodeEnum;
import com.tiv.image.hub.common.BusinessResponse;
import com.tiv.image.hub.common.DeleteRequest;
import com.tiv.image.hub.model.dto.space.user.SpaceUserAddRequest;
import com.tiv.image.hub.model.dto.space.user.SpaceUserQueryRequest;
import com.tiv.image.hub.model.dto.space.user.SpaceUserUpdateRequest;
import com.tiv.image.hub.model.entity.SpaceUser;
import com.tiv.image.hub.model.entity.User;
import com.tiv.image.hub.model.vo.SpaceUserVO;
import com.tiv.image.hub.service.SpaceUserService;
import com.tiv.image.hub.service.UserService;
import com.tiv.image.hub.util.ResultUtils;
import com.tiv.image.hub.util.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * 空间成员管理
 */
@Slf4j
@RestController
@RequestMapping("/space/user")
public class SpaceUserController {

    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private UserService userService;

    /**
     * 添加空间成员
     */
    @PostMapping("/add")
    public BusinessResponse<Long> addSpaceUser(@RequestBody SpaceUserAddRequest spaceUserAddRequest,
                                               HttpServletRequest httpServletRequest) {
        return ResultUtils.success(spaceUserService.addSpaceUser(spaceUserAddRequest));
    }

    /**
     * 编辑空间成员
     */
    @PostMapping("/update")
    public BusinessResponse<Boolean> editSpaceUser(@RequestBody @Valid SpaceUserUpdateRequest spaceUserUpdateRequest,
                                                   HttpServletRequest httpServletRequest) {
        SpaceUser spaceUser = SpaceUser.builder()
                .id(spaceUserUpdateRequest.getId())
                .spaceRole(spaceUserUpdateRequest.getSpaceRole())
                .build();
        // 校验参数
        spaceUserService.validSpaceUser(spaceUser, false);

        return ResultUtils.success(spaceUserService.updateById(spaceUser));
    }

    /**
     * 移除空间成员
     */
    @DeleteMapping()
    public BusinessResponse<Boolean> deleteSpaceUser(@RequestBody @Valid DeleteRequest deleteRequest,
                                                     HttpServletRequest httpServletRequest) {
        SpaceUser spaceUser = spaceUserService.getById(deleteRequest.getId());
        ThrowUtils.throwIf(spaceUser == null, BusinessCodeEnum.NOT_FOUND_ERROR);

        return ResultUtils.success(spaceUserService.removeById(deleteRequest.getId()));
    }

    /**
     * 查询指定空间成员
     */
    @PostMapping("/vo")
    public BusinessResponse<SpaceUser> getSpaceUser(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest) {

        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();
        ThrowUtils.throwIf(ObjectUtil.hasEmpty(spaceId, userId), BusinessCodeEnum.PARAMS_ERROR);

        SpaceUser spaceUser = spaceUserService.getOne(spaceUserService.getQueryWrapper(spaceUserQueryRequest));
        ThrowUtils.throwIf(spaceUser == null, BusinessCodeEnum.NOT_FOUND_ERROR);
        return ResultUtils.success(spaceUser);
    }

    /**
     * 查询空间成员列表
     */
    @PostMapping("/vo/list")
    public BusinessResponse<List<SpaceUserVO>> listSpaceUser(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest,
                                                             HttpServletRequest httpServletRequest) {
        List<SpaceUser> spaceUserList = spaceUserService.list(
                spaceUserService.getQueryWrapper(spaceUserQueryRequest)
        );
        return ResultUtils.success(spaceUserService.getSpaceUserVOList(spaceUserList));
    }

    /**
     * 查询我加入的团队空间的成员列表
     */
    @PostMapping("/vo/list/my")
    public BusinessResponse<List<SpaceUserVO>> listMyTeamSpaceUser(HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        SpaceUserQueryRequest spaceUserQueryRequest = new SpaceUserQueryRequest();
        spaceUserQueryRequest.setUserId(loginUser.getId());

        List<SpaceUser> spaceUserList = spaceUserService.list(spaceUserService.getQueryWrapper(spaceUserQueryRequest));
        return ResultUtils.success(spaceUserService.getSpaceUserVOList(spaceUserList));
    }

}