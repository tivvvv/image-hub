package com.tiv.image.hub.manager.auth;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.tiv.image.hub.common.BusinessCodeEnum;
import com.tiv.image.hub.model.entity.*;
import com.tiv.image.hub.model.enums.SpaceRoleEnum;
import com.tiv.image.hub.model.enums.SpaceTypeEnum;
import com.tiv.image.hub.service.*;
import com.tiv.image.hub.util.ThrowUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 空间成员权限管理
 */
@Component
public class SpaceUserAuthManager {

    @Resource
    private SpaceRoleService spaceRoleService;

    @Resource
    private SpaceRolePermissionService spaceRolePermissionService;

    @Resource
    private SpacePermissionService spacePermissionService;

    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private UserService userService;

    /**
     * 根据空间角色获取权限标识列表
     *
     * @param spaceRole 空间角色标识 viewer/editor/admin
     * @return 权限标识列表
     */
    public List<String> getPermissionsByRole(String spaceRole) {
        if (StrUtil.isBlank(spaceRole)) {
            return new ArrayList<>();
        }
        // 1. 查询角色
        SpaceRole role = spaceRoleService.lambdaQuery()
                .eq(SpaceRole::getRoleKey, spaceRole)
                .one();
        if (role == null) {
            return new ArrayList<>();
        }
        // 2. 查询角色关联的权限id
        List<Long> permissionIds = spaceRolePermissionService.lambdaQuery()
                .eq(SpaceRolePermission::getRoleId, role.getId())
                .list()
                .stream()
                .map(SpaceRolePermission::getPermissionId)
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(permissionIds)) {
            return new ArrayList<>();
        }
        // 3. 查询权限标识
        return spacePermissionService.listByIds(permissionIds)
                .stream()
                .map(SpacePermission::getPermissionKey)
                .collect(Collectors.toList());
    }

    /**
     * 获取登录用户在指定空间的权限标识列表
     *
     * @param space     空间,为null表示公共图库
     * @param loginUser 登录用户
     * @return 权限标识列表
     */
    public List<String> getPermissionList(Space space, User loginUser) {
        if (loginUser == null) {
            return new ArrayList<>();
        }
        // 系统管理员拥有全部权限
        if (userService.isAdmin(loginUser)) {
            return getPermissionsByRole(SpaceRoleEnum.ADMIN.value);
        }
        // 公共图库,普通用户仅可查看
        if (space == null) {
            return getPermissionsByRole(SpaceRoleEnum.VIEWER.value);
        }
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(space.getSpaceType());
        if (spaceTypeEnum == null) {
            return new ArrayList<>();
        }
        switch (spaceTypeEnum) {
            case PRIVATE:
                // 私有空间,仅本人拥有全部权限
                if (space.getUserId().equals(loginUser.getId())) {
                    return getPermissionsByRole(SpaceRoleEnum.ADMIN.value);
                }
                return new ArrayList<>();
            case TEAM:
                // 团队空间,根据成员角色获取权限
                SpaceUser spaceUser = spaceUserService.lambdaQuery()
                        .eq(SpaceUser::getSpaceId, space.getId())
                        .eq(SpaceUser::getUserId, loginUser.getId())
                        .one();
                if (spaceUser == null) {
                    return new ArrayList<>();
                }
                return getPermissionsByRole(spaceUser.getSpaceRole());
            default:
                return new ArrayList<>();
        }
    }

    /**
     * 校验登录用户在指定空间是否拥有指定权限,无权限则抛出异常
     *
     * @param space      空间,为null表示公共图库
     * @param loginUser  登录用户
     * @param permission 权限标识
     */
    public void checkPermission(Space space, User loginUser, String permission) {
        ThrowUtils.throwIf(!getPermissionList(space, loginUser).contains(permission),
                BusinessCodeEnum.NO_AUTH_ERROR);
    }

}
