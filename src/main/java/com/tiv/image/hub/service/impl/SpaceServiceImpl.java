package com.tiv.image.hub.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tiv.image.hub.common.BusinessCodeEnum;
import com.tiv.image.hub.exception.BusinessException;
import com.tiv.image.hub.mapper.SpaceMapper;
import com.tiv.image.hub.model.dto.space.SpaceAddRequest;
import com.tiv.image.hub.model.dto.space.SpaceQueryRequest;
import com.tiv.image.hub.model.entity.Space;
import com.tiv.image.hub.model.entity.User;
import com.tiv.image.hub.model.enums.SpaceLevelEnum;
import com.tiv.image.hub.model.vo.SpaceVO;
import com.tiv.image.hub.model.vo.UserVO;
import com.tiv.image.hub.service.SpaceService;
import com.tiv.image.hub.service.UserService;
import com.tiv.image.hub.util.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space> implements SpaceService {


    @Resource
    private UserService userService;

    private static final int SPACE_NAME_MAX_LENGTH = 50;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public void validateSpace(Space space, boolean isAdd) {
        ThrowUtils.throwIf(space == null, BusinessCodeEnum.PARAMS_ERROR);
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);

        if (isAdd) {
            // 新建时校验
            ThrowUtils.throwIf(StrUtil.isBlank(spaceName), BusinessCodeEnum.PARAMS_ERROR, "空间名称不能为空");
            ThrowUtils.throwIf(spaceName.length() > SPACE_NAME_MAX_LENGTH,
                    BusinessCodeEnum.PARAMS_ERROR, "空间名称过长");

            ThrowUtils.throwIf(spaceLevelEnum == null, BusinessCodeEnum.PARAMS_ERROR, "空间级别不能为空");
        } else {
            // 更新时校验
            ThrowUtils.throwIf(space.getId() == null, BusinessCodeEnum.PARAMS_ERROR, "id不能为空");

            // 如果更新空间名称
            ThrowUtils.throwIf(StrUtil.isNotBlank(spaceName) && spaceName.length() > SPACE_NAME_MAX_LENGTH,
                    BusinessCodeEnum.PARAMS_ERROR, "空间名称过长");

            // 如果更新空间级别
            ThrowUtils.throwIf(spaceLevel != null && spaceLevelEnum == null,
                    BusinessCodeEnum.PARAMS_ERROR, "空间级别不存在");
        }
    }

    @Override
    public SpaceVO addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        // 1. 校验参数
        Space space = new Space();
        BeanUtil.copyProperties(spaceAddRequest, space);
        if (space.getSpaceName() == null) {
            space.setSpaceName(String.format("%s的私人空间", loginUser.getUserName()));
        }
        validateSpace(space, true);

        // 2. 校验权限
        Long userId = loginUser.getId();
        space.setUserId(userId);
        if (space.getSpaceLevel() != SpaceLevelEnum.COMMON.getValue() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(BusinessCodeEnum.NO_AUTH_ERROR, "无权限创建指定级别的空间");
        }

        // 3. 填充默认配额
        populateQuotaBySpaceLevel(space);

        // 4. 更新库表
        // 每个用户只能有一个空间,锁+事务
        String lock = String.valueOf(userId).intern();
        synchronized (lock) {
            transactionTemplate.execute(status -> {
                // 判断是否已有空间
                boolean exists = this.lambdaQuery()
                        .eq(Space::getUserId, userId)
                        .exists();
                ThrowUtils.throwIf(exists, BusinessCodeEnum.FORBIDDEN_ERROR, "每个用户最多创建一个空间");
                // 创建
                boolean result = this.save(space);
                ThrowUtils.throwIf(!result, BusinessCodeEnum.SYSTEM_ERROR, "保存空间失败");

                return space;
            });
        }
        return SpaceVO.transferToVO(space);
    }

    @Override
    public SpaceVO getSpaceVO(Space space) {
        SpaceVO spaceVO = SpaceVO.transferToVO(space);
        Long userId = space.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            spaceVO.setUserVO(userVO);
        }
        return spaceVO;
    }

    @Override
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage) {
        List<Space> spaceList = spacePage.getRecords();
        Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        if (CollUtil.isEmpty(spaceList)) {
            return spaceVOPage;
        }
        // 转换成包装类
        List<SpaceVO> spaceVOList = spaceList.stream()
                .map(SpaceVO::transferToVO)
                .collect(Collectors.toList());
        Set<Long> userIdSet = spaceList.stream()
                .map(Space::getUserId)
                .collect(Collectors.toSet());
        // 查询用户信息
        List<User> userList = userService.listByIds(userIdSet);
        // 用户id -> 用户信息的映射
        Map<Long, User> userIdToUserMap = userList.stream()
                .collect(Collectors.toMap(
                        User::getId,
                        user -> user,
                        (existing, replacement) -> replacement)
                );
        // 添加用户信息
        spaceVOList.forEach(spaceVO -> {
            Long userId = spaceVO.getUserId();
            if (userIdToUserMap.containsKey(userId)) {
                User user = userIdToUserMap.get(userId);
                spaceVO.setUserVO(userService.getUserVO(user));
            }
        });
        spaceVOPage.setRecords(spaceVOList);
        return spaceVOPage;
    }

    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        ThrowUtils.throwIf(spaceQueryRequest == null, BusinessCodeEnum.PARAMS_ERROR, "请求参数为空");
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq(spaceQueryRequest.getId() != null, "id", spaceQueryRequest.getId());
        queryWrapper.like(StrUtil.isNotBlank(spaceQueryRequest.getSpaceName()), "space_name", spaceQueryRequest.getSpaceName());
        queryWrapper.eq(spaceQueryRequest.getSpaceLevel() != null, "space_level", spaceQueryRequest.getSpaceLevel());
        queryWrapper.eq(spaceQueryRequest.getUserId() != null, "user_id", spaceQueryRequest.getUserId());

        queryWrapper.orderBy(StrUtil.isNotBlank(spaceQueryRequest.getSortField()),
                "asc".equals(spaceQueryRequest.getSortOrder()), spaceQueryRequest.getSortField());

        return queryWrapper;
    }

    @Override
    public void populateQuotaBySpaceLevel(Space space) {
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        ThrowUtils.throwIf(spaceLevelEnum == null, BusinessCodeEnum.PARAMS_ERROR, "空间级别异常");

        if (space.getMaxSize() == null) {
            space.setMaxSize(spaceLevelEnum.getBaseMaxSize());
        }
        if (space.getMaxCount() == null) {
            space.setMaxCount(spaceLevelEnum.getBaseMaxCount());
        }
    }

}