package com.tiv.image.hub.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tiv.image.hub.common.BusinessCodeEnum;
import com.tiv.image.hub.mapper.UserMapper;
import com.tiv.image.hub.model.dto.user.UserQueryRequest;
import com.tiv.image.hub.model.entity.User;
import com.tiv.image.hub.model.enums.UserRoleEnum;
import com.tiv.image.hub.model.vo.LoginUserVO;
import com.tiv.image.hub.model.vo.UserVO;
import com.tiv.image.hub.service.UserService;
import com.tiv.image.hub.util.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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
        validateParam(userAccount, userPassword);

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

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword) {

        // 1. 校验参数
        validateParam(userAccount, userPassword);

        // 2. 密码加密
        String encryptedPassword = encryptPassword(userPassword);

        // 3. 查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        queryWrapper.eq("user_password", encryptedPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        ThrowUtils.throwIf(user == null, BusinessCodeEnum.PARAMS_ERROR, "用户不存在或者密码错误");
        // 4. 封禁用户禁止登录
        ThrowUtils.throwIf(UserRoleEnum.BANNED.getValue().equals(user.getUserRole()),
                BusinessCodeEnum.NO_AUTH_ERROR, "账号已被封禁");

        // 5. 保存用户登录态
        StpUtil.login(user.getId());
        return getLoginUserVO(user);
    }

    @Override
    public User getLoginUser() {

        // 1. 校验用户登录态
        StpUtil.checkLogin();

        // 2. 从数据库查询用户
        User currentUser = this.getById(StpUtil.getLoginIdAsLong());
        ThrowUtils.throwIf(currentUser == null, BusinessCodeEnum.NOT_LOGIN_ERROR);

        // 3. 封禁用户视为无权限
        ThrowUtils.throwIf(UserRoleEnum.BANNED.getValue().equals(currentUser.getUserRole()),
                BusinessCodeEnum.NO_AUTH_ERROR, "账号已被封禁");

        return currentUser;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return Collections.emptyList();
        }
        return userList.stream()
                .map(this::getUserVO)
                .collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, BusinessCodeEnum.PARAMS_ERROR, "请求参数为空");
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq(userQueryRequest.getId() != null, "id", userQueryRequest.getId());
        queryWrapper.like(StrUtil.isNotBlank(userQueryRequest.getUserAccount()), "user_account", userQueryRequest.getUserAccount());
        queryWrapper.like(StrUtil.isNotBlank(userQueryRequest.getUserName()), "user_name", userQueryRequest.getUserName());
        queryWrapper.like(StrUtil.isNotBlank(userQueryRequest.getUserProfile()), "user_profile", userQueryRequest.getUserProfile());
        queryWrapper.eq(StrUtil.isNotBlank(userQueryRequest.getUserRole()), "user_role", userQueryRequest.getUserRole());
        queryWrapper.orderBy(StrUtil.isNotBlank(userQueryRequest.getSortField()), "asc".equals(userQueryRequest.getSortOrder()), userQueryRequest.getSortField());

        return queryWrapper;
    }

    @Override
    public void userLogout() {
        StpUtil.logout();
    }

    @Override
    public boolean isAdmin(User user) {
        if (user == null) {
            return false;
        }
        return UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 校验参数
     *
     * @param userAccount
     * @param userPassword
     */
    private void validateParam(String userAccount, String userPassword) {
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword), BusinessCodeEnum.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(userAccount.length() < 4, BusinessCodeEnum.PARAMS_ERROR, "用户账号过短");
        ThrowUtils.throwIf(userPassword.length() < 8, BusinessCodeEnum.PARAMS_ERROR, "用户密码过短");
    }

}