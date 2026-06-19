package com.tiv.image.hub.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tiv.image.hub.common.BusinessCodeEnum;
import com.tiv.image.hub.mapper.SpaceUserMapper;
import com.tiv.image.hub.model.dto.space.user.SpaceUserAddRequest;
import com.tiv.image.hub.model.dto.space.user.SpaceUserQueryRequest;
import com.tiv.image.hub.model.entity.Space;
import com.tiv.image.hub.model.entity.SpaceUser;
import com.tiv.image.hub.model.entity.User;
import com.tiv.image.hub.model.enums.SpaceRoleEnum;
import com.tiv.image.hub.model.vo.SpaceUserVO;
import com.tiv.image.hub.service.SpaceService;
import com.tiv.image.hub.service.SpaceUserService;
import com.tiv.image.hub.service.UserService;
import com.tiv.image.hub.util.ThrowUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser> implements SpaceUserService {

    @Resource
    private UserService userService;

    @Lazy
    @Resource
    private SpaceService spaceService;

    @Override
    public void validSpaceUser(SpaceUser spaceUser, boolean isAdd) {
        ThrowUtils.throwIf(spaceUser == null, BusinessCodeEnum.PARAMS_ERROR);
        Long spaceId = spaceUser.getSpaceId();
        Long userId = spaceUser.getUserId();
        if (isAdd) {
            ThrowUtils.throwIf(ObjectUtil.hasEmpty(spaceId, userId), BusinessCodeEnum.PARAMS_ERROR);
            User user = userService.getById(userId);
            ThrowUtils.throwIf(user == null, BusinessCodeEnum.NOT_FOUND_ERROR, "用户不存在");
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, BusinessCodeEnum.NOT_FOUND_ERROR, "空间不存在");
        }
        // 校验空间角色
        String spaceRole = spaceUser.getSpaceRole();
        SpaceRoleEnum spaceRoleEnum = SpaceRoleEnum.getEnumByValue(spaceRole);
        ThrowUtils.throwIf(spaceRoleEnum == null, BusinessCodeEnum.PARAMS_ERROR, "空间角色不存在");
        // 校验空间用户是否存在
        if (spaceUser.getId() != null) {
            ThrowUtils.throwIf(getById(spaceUser.getId()) == null, BusinessCodeEnum.NOT_FOUND_ERROR);
        }
    }

    @Override
    public long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest) {
        // 参数校验
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserAddRequest, spaceUser);
        validSpaceUser(spaceUser, true);

        // 更新库表
        boolean result = this.save(spaceUser);
        ThrowUtils.throwIf(!result, BusinessCodeEnum.OPERATION_ERROR);
        return spaceUser.getId();
    }

    @Override
    public SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request) {
        // 实体类转封装类
        SpaceUserVO spaceUserVO = SpaceUserVO.transferToVO(spaceUser);
        // 关联查询用户信息
        Long userId = spaceUser.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            spaceUserVO.setUserVO(userService.getUserVO(user));
        }
        // 关联查询空间信息
        Long spaceId = spaceUser.getSpaceId();
        if (spaceId != null && spaceId > 0) {
            Space space = spaceService.getById(spaceId);
            spaceUserVO.setSpaceVO(spaceService.getSpaceVO(space));
        }
        return spaceUserVO;
    }

    @Override
    public List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList) {
        if (CollUtil.isEmpty(spaceUserList)) {
            return Collections.emptyList();
        }
        // 1. 实体类转封装类
        List<SpaceUserVO> spaceUserVOList = spaceUserList.stream()
                .map(SpaceUserVO::transferToVO)
                .collect(Collectors.toList());

        // 2. 批量查询用户
        Set<Long> userIdSet = spaceUserList.stream()
                .map(SpaceUser::getUserId)
                .collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet)
                .stream()
                .collect(Collectors.groupingBy(User::getId));

        // 3. 批量查询空间
        Set<Long> spaceIdSet = spaceUserList.stream()
                .map(SpaceUser::getSpaceId)
                .collect(Collectors.toSet());
        Map<Long, List<Space>> spaceIdSpaceListMap = spaceService.listByIds(spaceIdSet)
                .stream()
                .collect(Collectors.groupingBy(Space::getId));

        // 4. 填充SpaceUserVO的用户和空间信息
        spaceUserVOList.forEach(spaceUserVO -> {
            Long userId = spaceUserVO.getUserId();
            Long spaceId = spaceUserVO.getSpaceId();

            // 填充用户信息
            if (userIdUserListMap.containsKey(userId)) {
                User user = userIdUserListMap.get(userId).get(0);
                spaceUserVO.setUserVO(userService.getUserVO(user));
            }
            // 填充空间信息
            if (spaceIdSpaceListMap.containsKey(spaceId)) {
                Space space = spaceIdSpaceListMap.get(spaceId).get(0);
                spaceUserVO.setSpaceVO(spaceService.getSpaceVO(space));
            }
        });
        return spaceUserVOList;
    }

    @Override
    public QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest) {
        ThrowUtils.throwIf(spaceUserQueryRequest == null, BusinessCodeEnum.PARAMS_ERROR, "请求参数为空");
        QueryWrapper<SpaceUser> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq(ObjUtil.isNotEmpty(spaceUserQueryRequest.getId()), "id", spaceUserQueryRequest.getId());
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceUserQueryRequest.getSpaceId()), "space_id", spaceUserQueryRequest.getSpaceId());
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceUserQueryRequest.getUserId()), "user_id", spaceUserQueryRequest.getUserId());
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceUserQueryRequest.getSpaceRole()), "space_role", spaceUserQueryRequest.getSpaceRole());

        return queryWrapper;
    }

}