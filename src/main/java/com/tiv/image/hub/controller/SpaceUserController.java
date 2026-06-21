package com.tiv.image.hub.controller;

import cn.hutool.core.util.ObjectUtil;
import com.tiv.image.hub.common.BusinessCodeEnum;
import com.tiv.image.hub.common.BusinessResponse;
import com.tiv.image.hub.common.DeleteRequest;
import com.tiv.image.hub.constant.SpaceUserPermissionKeys;
import com.tiv.image.hub.manager.auth.SpaceUserAuthManager;
import com.tiv.image.hub.model.dto.space.user.SpaceUserAddRequest;
import com.tiv.image.hub.model.dto.space.user.SpaceUserQueryRequest;
import com.tiv.image.hub.model.dto.space.user.SpaceUserUpdateRequest;
import com.tiv.image.hub.model.entity.Space;
import com.tiv.image.hub.model.entity.SpaceUser;
import com.tiv.image.hub.model.entity.User;
import com.tiv.image.hub.model.enums.SpaceRoleEnum;
import com.tiv.image.hub.model.vo.SpaceUserVO;
import com.tiv.image.hub.service.SpaceService;
import com.tiv.image.hub.service.SpaceUserService;
import com.tiv.image.hub.service.UserService;
import com.tiv.image.hub.util.ResultUtils;
import com.tiv.image.hub.util.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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

    @Resource
    private SpaceService spaceService;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;

    /**
     * 添加空间成员
     */
    @PostMapping("/add")
    public BusinessResponse<Long> addSpaceUser(@RequestBody SpaceUserAddRequest spaceUserAddRequest) {
        // 校验成员管理权限
        User loginUser = userService.getLoginUser();
        checkSpaceUserManageAuth(spaceUserAddRequest.getSpaceId(), loginUser);
        return ResultUtils.success(spaceUserService.addSpaceUser(spaceUserAddRequest));
    }

    /**
     * 编辑空间成员
     */
    @PostMapping("/update")
    public BusinessResponse<Boolean> editSpaceUser(@RequestBody @Valid SpaceUserUpdateRequest spaceUserUpdateRequest) {
        // 校验空间角色合法性
        String spaceRole = spaceUserUpdateRequest.getSpaceRole();
        ThrowUtils.throwIf(SpaceRoleEnum.getEnumByValue(spaceRole) == null,
                BusinessCodeEnum.PARAMS_ERROR, "空间角色不存在");
        // 校验成员是否存在
        SpaceUser oldSpaceUser = spaceUserService.getById(spaceUserUpdateRequest.getId());
        ThrowUtils.throwIf(oldSpaceUser == null, BusinessCodeEnum.NOT_FOUND_ERROR, "空间成员不存在");
        // 校验成员管理权限
        User loginUser = userService.getLoginUser();
        checkSpaceUserManageAuth(oldSpaceUser.getSpaceId(), loginUser);

        // 更新空间角色
        SpaceUser spaceUser = SpaceUser.builder()
                .id(spaceUserUpdateRequest.getId())
                .spaceRole(spaceRole)
                .build();
        return ResultUtils.success(spaceUserService.updateById(spaceUser));
    }

    /**
     * 移除空间成员
     */
    @DeleteMapping()
    public BusinessResponse<Boolean> deleteSpaceUser(@RequestBody @Valid DeleteRequest deleteRequest) {
        SpaceUser spaceUser = spaceUserService.getById(deleteRequest.getId());
        ThrowUtils.throwIf(spaceUser == null, BusinessCodeEnum.NOT_FOUND_ERROR);
        // 校验成员管理权限
        User loginUser = userService.getLoginUser();
        checkSpaceUserManageAuth(spaceUser.getSpaceId(), loginUser);

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
        // 校验成员管理权限
        User loginUser = userService.getLoginUser();
        checkSpaceUserManageAuth(spaceId, loginUser);

        SpaceUser spaceUser = spaceUserService.getOne(spaceUserService.getQueryWrapper(spaceUserQueryRequest));
        ThrowUtils.throwIf(spaceUser == null, BusinessCodeEnum.NOT_FOUND_ERROR);
        return ResultUtils.success(spaceUser);
    }

    /**
     * 查询空间成员列表
     */
    @PostMapping("/vo/list")
    public BusinessResponse<List<SpaceUserVO>> listSpaceUser(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest) {
        // 校验成员管理权限
        User loginUser = userService.getLoginUser();
        checkSpaceUserManageAuth(spaceUserQueryRequest.getSpaceId(), loginUser);

        List<SpaceUser> spaceUserList = spaceUserService.list(
                spaceUserService.getQueryWrapper(spaceUserQueryRequest)
        );
        return ResultUtils.success(spaceUserService.getSpaceUserVOList(spaceUserList));
    }

    /**
     * 查询我加入的团队空间的成员列表
     */
    @PostMapping("/vo/list/my")
    public BusinessResponse<List<SpaceUserVO>> listMyTeamSpaceUser() {
        User loginUser = userService.getLoginUser();
        SpaceUserQueryRequest spaceUserQueryRequest = new SpaceUserQueryRequest();
        spaceUserQueryRequest.setUserId(loginUser.getId());

        List<SpaceUser> spaceUserList = spaceUserService.list(spaceUserService.getQueryWrapper(spaceUserQueryRequest));
        return ResultUtils.success(spaceUserService.getSpaceUserVOList(spaceUserList));
    }

    /**
     * 校验登录用户是否拥有指定空间的成员管理权限
     *
     * @param spaceId   空间id
     * @param loginUser 登录用户
     */
    private void checkSpaceUserManageAuth(Long spaceId, User loginUser) {
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, BusinessCodeEnum.NOT_FOUND_ERROR, "空间不存在");
        spaceUserAuthManager.checkPermission(space, loginUser, SpaceUserPermissionKeys.SPACE_USER_MANAGE);
    }

}