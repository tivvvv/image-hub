package com.tiv.image.hub.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tiv.image.hub.common.BusinessCodeEnum;
import com.tiv.image.hub.mapper.UserMapper;
import com.tiv.image.hub.model.entity.User;
import com.tiv.image.hub.model.enums.UserRoleEnum;
import com.tiv.image.hub.service.UserService;
import com.tiv.image.hub.util.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final String SALT = "tivvvv";

    @Override
    public String encryptPassword(String password) {

        return DigestUtils.md5DigestAsHex((password + SALT).getBytes());
    }

    @Override
    public long userRegister(String userAccount, String userPassword) {

        // 1. 校验参数
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword), BusinessCodeEnum.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(userAccount.length() < 4, BusinessCodeEnum.PARAMS_ERROR, "用户账号过短");
        ThrowUtils.throwIf(userPassword.length() < 8, BusinessCodeEnum.PARAMS_ERROR, "用户密码过短");

        // 2. 检查账号重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        long count = this.baseMapper.selectCount(queryWrapper);
        ThrowUtils.throwIf(count > 0, BusinessCodeEnum.PARAMS_ERROR, "用户账号重复");

        // 3. 密码加密
        String encryptedPassword = encryptPassword(userPassword);

        // 4. 保存数据
        User user = User.builder()
                .userAccount(userAccount)
                .userPassword(encryptedPassword)
                .userName("无名")
                .userRole(UserRoleEnum.USER.getValue())
                .build();
        boolean saveResult = this.save(user);
        ThrowUtils.throwIf(!saveResult, BusinessCodeEnum.SYSTEM_ERROR, "注册失败");
        return user.getId();
    }

}