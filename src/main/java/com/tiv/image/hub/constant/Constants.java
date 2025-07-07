package com.tiv.image.hub.constant;

import java.util.List;

/**
 * 通用常量
 */
public interface Constants {

    String USER_LOGIN_STATE = "user_login_state";

    String ADMIN_ROLE = "admin";

    String DEFAULT_PASSWORD = "12345678";

    long ONE_MEGA_BYTES = 1024 * 1024;

    List<String> VALID_IMAGE_SUFFIXES = List.of("png", "jpg", "jpeg", "gif", "webp");

}
