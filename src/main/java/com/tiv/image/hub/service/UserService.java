package com.tiv.image.hub.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tiv.image.hub.model.dto.user.UserQueryRequest;
import com.tiv.image.hub.model.entity.User;
import com.tiv.image.hub.model.vo.LoginUserVO;
import com.tiv.image.hub.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface UserService extends IService<User> {

    /**
     * 密码加密
     *
     * @param password
     * @return
     */
    String encryptPassword(String password);

    /**
     * 用户注册
     *
     * @param userAccount
     * @param userPassword
     * @return
     */
    long userRegister(String userAccount, String userPassword);

    /**
     * 用户登录
     *
     * @param userAccount
     * @param userPassword
     * @param httpServletRequest
     * @return
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest httpServletRequest);

    /**
     * 获取当前登录用户
     *
     * @param httpServletRequest
     * @return
     */
    User getLoginUser(HttpServletRequest httpServletRequest);

    /**
     * 获取当前登录用户视图(脱敏)
     *
     * @param user
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取用户视图(脱敏)
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取用户视图(脱敏)列表
     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 获取用户查询条件
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 用户登出
     *
     * @param httpServletRequest
     */
    void userLogout(HttpServletRequest httpServletRequest);

}