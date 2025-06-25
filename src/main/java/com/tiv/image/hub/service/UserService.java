package com.tiv.image.hub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tiv.image.hub.model.entity.User;

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

}