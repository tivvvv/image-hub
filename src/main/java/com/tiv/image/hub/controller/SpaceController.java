package com.tiv.image.hub.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tiv.image.hub.annotation.AuthCheck;
import com.tiv.image.hub.common.BusinessCodeEnum;
import com.tiv.image.hub.common.BusinessResponse;
import com.tiv.image.hub.common.DeleteRequest;
import com.tiv.image.hub.constant.Constants;
import com.tiv.image.hub.model.dto.space.SpaceAddRequest;
import com.tiv.image.hub.model.dto.space.SpaceQueryRequest;
import com.tiv.image.hub.model.dto.space.SpaceUpdateRequest;
import com.tiv.image.hub.model.entity.Space;
import com.tiv.image.hub.model.entity.User;
import com.tiv.image.hub.model.vo.SpaceVO;
import com.tiv.image.hub.service.SpaceService;
import com.tiv.image.hub.service.UserService;
import com.tiv.image.hub.util.ResultUtils;
import com.tiv.image.hub.util.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 空间controller
 */
@Slf4j
@RestController
@RequestMapping("/space")
public class SpaceController {

    @Resource
    private SpaceService spaceService;

    @Resource
    private UserService userService;

    private static final int USER_QUERY_SPACE_LIMIT = 10;

    /**
     * 创建空间
     *
     * @param spaceAddRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/add")
    public BusinessResponse<SpaceVO> addSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(spaceService.addSpace(spaceAddRequest, loginUser));
    }

    /**
     * 更新空间
     *
     * @param spaceUpdateRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/update")
    public BusinessResponse<Boolean> updateSpace(@RequestBody @Valid SpaceUpdateRequest spaceUpdateRequest, HttpServletRequest httpServletRequest) {
        // 判断空间是否存在
        long id = spaceUpdateRequest.getId();
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, BusinessCodeEnum.NOT_FOUND_ERROR);

        User loginUser = userService.getLoginUser(httpServletRequest);
        // 仅创建人或管理员可更新空间
        ThrowUtils.throwIf(!loginUser.getId().equals(oldSpace.getUserId())
                && !userService.isAdmin(loginUser), BusinessCodeEnum.NO_AUTH_ERROR);

        Space space = new Space();
        BeanUtils.copyProperties(spaceUpdateRequest, space);

        // 校验图片参数
        spaceService.validateSpace(space, false);

        // 填充配额参数
        spaceService.populateQuotaBySpaceLevel(space);

        // 更新库表
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, BusinessCodeEnum.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据id获取空间视图
     */
    @GetMapping("/vo/{id}")
    public BusinessResponse<SpaceVO> getSpaceVOById(@PathVariable long id) {
        Space space = doGetSpace(id);
        // 获取封装类
        return ResultUtils.success(spaceService.getSpaceVO(space));
    }

    /**
     * 分页获取空间视图列表
     */
    @PostMapping("/page/vo")
    public BusinessResponse<Page<SpaceVO>> listSpaceVOByPage(@RequestBody SpaceQueryRequest spaceQueryRequest) {
        ThrowUtils.throwIf(spaceQueryRequest.getPageSize() > USER_QUERY_SPACE_LIMIT,
                BusinessCodeEnum.PARAMS_ERROR, "查询数量过多");
        Page<Space> spacePage = doListSpace(spaceQueryRequest);
        // 获取封装类
        return ResultUtils.success(spaceService.getSpaceVOPage(spacePage));
    }

    /**
     * 删除空间
     *
     * @param deleteRequest
     * @param httpServletRequest
     * @return
     */
    @DeleteMapping()
    public BusinessResponse<Boolean> deleteSpace(@RequestBody @Valid DeleteRequest deleteRequest,
                                                 HttpServletRequest httpServletRequest) {
        Space space = spaceService.getById(deleteRequest.getId());
        ThrowUtils.throwIf(space == null, BusinessCodeEnum.NOT_FOUND_ERROR);

        User loginUser = userService.getLoginUser(httpServletRequest);
        // 仅创建人或管理员可删除
        ThrowUtils.throwIf(!loginUser.getId().equals(space.getUserId())
                && !userService.isAdmin(loginUser), BusinessCodeEnum.NO_AUTH_ERROR);
        boolean result = spaceService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result, BusinessCodeEnum.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 管理员根据id获取空间
     */
    @GetMapping("/{id}")
    @AuthCheck(mustRole = Constants.ADMIN_ROLE)
    public BusinessResponse<Space> getSpaceById(@PathVariable long id) {
        return ResultUtils.success(doGetSpace(id));
    }

    /**
     * 管理员分页获取空间列表
     */
    @PostMapping("/page")
    @AuthCheck(mustRole = Constants.ADMIN_ROLE)
    public BusinessResponse<Page<Space>> listSpaceByPage(@RequestBody SpaceQueryRequest spaceQueryRequest) {
        return ResultUtils.success(doListSpace(spaceQueryRequest));
    }

    private Space doGetSpace(long id) {
        ThrowUtils.throwIf(id <= 0, BusinessCodeEnum.PARAMS_ERROR);
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, BusinessCodeEnum.NOT_FOUND_ERROR);
        return space;
    }

    private Page<Space> doListSpace(SpaceQueryRequest spaceQueryRequest) {
        long current = spaceQueryRequest.getCurrent();
        long size = spaceQueryRequest.getPageSize();

        QueryWrapper<Space> queryWrapper = spaceService.getQueryWrapper(spaceQueryRequest);
        return spaceService.page(new Page<>(current, size), queryWrapper);
    }

}
