package com.tiv.image.hub.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tiv.image.hub.annotation.AuthCheck;
import com.tiv.image.hub.common.BusinessCodeEnum;
import com.tiv.image.hub.common.BusinessResponse;
import com.tiv.image.hub.common.DeleteRequest;
import com.tiv.image.hub.constant.Constants;
import com.tiv.image.hub.exception.BusinessException;
import com.tiv.image.hub.model.dto.user.*;
import com.tiv.image.hub.model.entity.User;
import com.tiv.image.hub.model.vo.LoginUserVO;
import com.tiv.image.hub.model.vo.UserVO;
import com.tiv.image.hub.service.UserService;
import com.tiv.image.hub.util.ResultUtils;
import com.tiv.image.hub.util.ThrowUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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
    public BusinessResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, BusinessCodeEnum.PARAMS_ERROR);
        long userId = userService.userRegister(userRegisterRequest.getUserAccount(), userRegisterRequest.getUserPassword());
        return ResultUtils.success(userId);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/login")
    public BusinessResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(userLoginRequest == null, BusinessCodeEnum.PARAMS_ERROR);
        LoginUserVO loginUserVO = userService.userLogin(userLoginRequest.getUserAccount(), userLoginRequest.getUserPassword(), httpServletRequest);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 获取当前登录用户
     *
     * @param httpServletRequest
     * @return
     */
    @GetMapping("/login")
    public BusinessResponse<LoginUserVO> getLoginUser(HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }

    /**
     * 用户登出
     *
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/logout")
    public BusinessResponse<Void> userLogout(HttpServletRequest httpServletRequest) {
        userService.userLogout(httpServletRequest);
        return ResultUtils.success(null);
    }

    /**
     * 管理员创建用户
     *
     * @param userAddRequest
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = Constants.ADMIN_ROLE)
    public BusinessResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, BusinessCodeEnum.PARAMS_ERROR);
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
    @AuthCheck(mustRole = Constants.ADMIN_ROLE)
    public BusinessResponse<User> getUserById(@PathVariable long id) {
        ThrowUtils.throwIf(id <= 0, BusinessCodeEnum.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, BusinessCodeEnum.NOT_FOUND_ERROR);
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
        BusinessResponse<User> response = getUserById(id);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 管理员删除用户
     *
     * @param deleteRequest
     * @return
     */
    @DeleteMapping()
    @AuthCheck(mustRole = Constants.ADMIN_ROLE)
    public BusinessResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, BusinessCodeEnum.PARAMS_ERROR);
        boolean result = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 管理员更新用户
     *
     * @param userUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = Constants.ADMIN_ROLE)
    public BusinessResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(BusinessCodeEnum.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, BusinessCodeEnum.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 管理员分页获取用户封装列表
     *
     * @param userQueryRequest
     * @return
     */
    @PostMapping("/page/vo")
    @AuthCheck(mustRole = Constants.ADMIN_ROLE)
    public BusinessResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, BusinessCodeEnum.PARAMS_ERROR);
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
