package com.tiv.image.hub.model.dto.user;

import com.tiv.image.hub.common.PageRequest;
import lombok.*;

import java.io.Serializable;

/**
 * 用户查询请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserQueryRequest extends PageRequest implements Serializable {

    /**
     * 用户id
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色:user/vip/banned/admin
     */
    private String userRole;

    private static final long serialVersionUID = 1L;

}
