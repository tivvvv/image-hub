package com.tiv.image.hub.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tiv.image.hub.common.BusinessCodeEnum;
import com.tiv.image.hub.common.BusinessResponse;
import com.tiv.image.hub.common.DeleteRequest;
import com.tiv.image.hub.constant.Constants;
import com.tiv.image.hub.model.dto.user.*;
import com.tiv.image.hub.model.entity.User;
import com.tiv.image.hub.model.vo.LoginUserVO;
import com.tiv.image.hub.model.vo.UserVO;
import com.tiv.image.hub.service.UserService;
import com.tiv.image.hub.util.ResultUtils;
import com.tiv.image.hub.util.ThrowUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 用户controller
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BusinessResponse<Long> userRegister(@RequestBody @Valid UserRegisterRequest userRegisterRequest) {
        long userId = userService.userRegister(userRegisterRequest.getUserAccount(), userRegisterRequest.getUserPassword());
        return ResultUtils.success(userId);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @return
     */
    @PostMapping("/login")
    public BusinessResponse<LoginUserVO> userLogin(@RequestBody @Valid UserLoginRequest userLoginRequest) {
        LoginUserVO loginUserVO = userService.userLogin(userLoginRequest.getUserAccount(), userLoginRequest.getUserPassword());
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 获取当前登录用户
     *
     * @return
     */
    @GetMapping("/login")
    public BusinessResponse<LoginUserVO> getLoginUser() {
        User loginUser = userService.getLoginUser();
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }

    /**
     * 用户登出
     *
     * @return
     */
    @PostMapping("/logout")
    public BusinessResponse<Void> userLogout() {
        userService.userLogout();
        return ResultUtils.success(null);
    }

    /**
     * 管理员创建用户
     *
     * @param userAddRequest
     * @return
     */
    @PostMapping("/add")
    @SaCheckRole(Constants.ADMIN_ROLE)
    public BusinessResponse<Long> addUser(@RequestBody @Valid UserAddRequest userAddRequest) {
        User user = new User();
        BeanUtil.copyProperties(userAddRequest, user);

        // 使用默认密码
        String encryptedPassword = userService.encryptPassword(Constants.DEFAULT_PASSWORD);
        user.setUserPassword(encryptedPassword);

        // 保存
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, BusinessCodeEnum.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 管理员根据id获取用户
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @SaCheckRole(Constants.ADMIN_ROLE)
    public BusinessResponse<User> getUserById(@PathVariable long id) {
        ThrowUtils.throwIf(id <= 0, BusinessCodeEnum.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, BusinessCodeEnum.NOT_FOUND_ERROR);
        // 隐藏密码
        user.setUserPassword(null);
        return ResultUtils.success(user);
    }

    /**
     * 根据id获取用户视图
     *
     * @param id
     * @return
     */
    @GetMapping("/vo/{id}")
    public BusinessResponse<UserVO> getUserVOById(@PathVariable long id) {
        // 需登录
        userService.getLoginUser();
        User user = getUserById(id).getData();
        // 隐藏账号
        user.setUserAccount(null);
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 管理员删除用户
     *
     * @param deleteRequest
     * @return
     */
    @DeleteMapping()
    @SaCheckRole(Constants.ADMIN_ROLE)
    public BusinessResponse<Boolean> deleteUser(@RequestBody @Valid DeleteRequest deleteRequest) {
        boolean result = userService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result, BusinessCodeEnum.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 管理员更新用户
     *
     * @param userUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @SaCheckRole(Constants.ADMIN_ROLE)
    public BusinessResponse<Boolean> updateUser(@RequestBody @Valid UserUpdateRequest userUpdateRequest) {
        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, BusinessCodeEnum.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 管理员分页获取用户视图列表
     *
     * @param userQueryRequest
     * @return
     */
    @PostMapping("/page/vo")
    @SaCheckRole(Constants.ADMIN_ROLE)
    public BusinessResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        long current = userQueryRequest.getCurrent();
        long pageSize = userQueryRequest.getPageSize();

        // 分页查询
        Page<User> userPage = userService.page(new Page<>(current, pageSize), userService.getQueryWrapper(userQueryRequest));
        // 转换为视图对象
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());

        Page<UserVO> userVOPage = new Page<>(current, pageSize, userPage.getTotal());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }

}
