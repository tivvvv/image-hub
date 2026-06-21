package com.tiv.image.hub.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tiv.image.hub.common.BusinessCodeEnum;
import com.tiv.image.hub.common.BusinessResponse;
import com.tiv.image.hub.common.DeleteRequest;
import com.tiv.image.hub.constant.Constants;
import com.tiv.image.hub.model.dto.space.SpaceAddRequest;
import com.tiv.image.hub.model.dto.space.SpaceQueryRequest;
import com.tiv.image.hub.model.dto.space.SpaceUpdateRequest;
import com.tiv.image.hub.model.entity.Space;
import com.tiv.image.hub.model.entity.User;
import com.tiv.image.hub.model.enums.SpaceLevelEnum;
import com.tiv.image.hub.model.vo.SpaceLevelVO;
import com.tiv.image.hub.model.vo.SpaceVO;
import com.tiv.image.hub.service.SpaceService;
import com.tiv.image.hub.service.UserService;
import com.tiv.image.hub.util.ResultUtils;
import com.tiv.image.hub.util.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
     * @return
     */
    @PostMapping("/add")
    public BusinessResponse<SpaceVO> addSpace(@RequestBody SpaceAddRequest spaceAddRequest) {
        User loginUser = userService.getLoginUser();
        return ResultUtils.success(spaceService.addSpace(spaceAddRequest, loginUser));
    }

    /**
     * 更新空间
     *
     * @param spaceUpdateRequest
     * @return
     */
    @PostMapping("/update")
    public BusinessResponse<SpaceVO> updateSpace(@RequestBody @Valid SpaceUpdateRequest spaceUpdateRequest) {
        Space oldSpace = spaceService.getById(spaceUpdateRequest.getId());
        User loginUser = userService.getLoginUser();
        // 仅创建人或管理员可更新空间
        spaceService.checkSpaceAuth(loginUser, oldSpace);

        Space space = new Space();
        BeanUtils.copyProperties(spaceUpdateRequest, space);

        // 校验图片参数
        spaceService.validateSpace(space, false);

        // 填充配额参数
        spaceService.populateQuotaBySpaceLevel(space);

        // 更新库表
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, BusinessCodeEnum.OPERATION_ERROR);
        return getSpaceVOById(spaceUpdateRequest.getId());
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
     * @return
     */
    @DeleteMapping()
    public BusinessResponse<Boolean> deleteSpace(@RequestBody @Valid DeleteRequest deleteRequest) {
        Space space = spaceService.getById(deleteRequest.getId());
        User loginUser = userService.getLoginUser();
        // 仅创建人或管理员可删除
        spaceService.checkSpaceAuth(loginUser, space);
        boolean result = spaceService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result, BusinessCodeEnum.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 获取空间级别列表
     *
     * @return
     */
    @GetMapping("/level")
    public BusinessResponse<List<SpaceLevelVO>> getSpaceLevel() {
        return ResultUtils.success(Arrays.stream(SpaceLevelEnum.values())
                .map(spaceLevelEnum -> new SpaceLevelVO(
                        spaceLevelEnum.getValue(),
                        spaceLevelEnum.getDesc(),
                        spaceLevelEnum.getBaseMaxSize(),
                        spaceLevelEnum.getBaseMaxCount()
                )).collect(Collectors.toList())
        );
    }

    /**
     * 管理员根据id获取空间
     */
    @GetMapping("/{id}")
    @SaCheckRole(Constants.ADMIN_ROLE)
    public BusinessResponse<Space> getSpaceById(@PathVariable long id) {
        return ResultUtils.success(doGetSpace(id));
    }

    /**
     * 管理员分页获取空间列表
     */
    @PostMapping("/page")
    @SaCheckRole(Constants.ADMIN_ROLE)
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
