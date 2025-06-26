package com.tiv.image.hub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tiv.image.hub.model.entity.User;
import com.tiv.image.hub.model.vo.LoginUserVO;

import javax.servlet.http.HttpServletRequest;

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
     * 用户登出
     *
     * @param httpServletRequest
     */
    void userLogout(HttpServletRequest httpServletRequest);

}