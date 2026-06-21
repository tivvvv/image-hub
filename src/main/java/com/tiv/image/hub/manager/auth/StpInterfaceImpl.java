package com.tiv.image.hub.manager.auth;

import cn.dev33.satoken.stp.StpInterface;
import com.tiv.image.hub.model.entity.User;
import com.tiv.image.hub.service.UserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * sa-token权限认证接口实现
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private UserService userService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return new ArrayList<>();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        User user = userService.getById(Long.valueOf(loginId.toString()));
        if (user == null) {
            return new ArrayList<>();
        }
        return Collections.singletonList(user.getUserRole());
    }

}
