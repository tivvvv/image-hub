-- 创建库
create database if not exists image_hub;

-- 切换库
use image_hub;

-- 用户表
create table if not exists user
(
    id            bigint                                 not null auto_increment comment '用户id',
    user_account  varchar(256)                           not null comment '账号',
    user_password varchar(512)                           not null comment '密码',
    user_name     varchar(256)                           null comment '用户昵称',
    user_avatar   varchar(512)                           null comment '用户头像',
    user_profile  varchar(512)                           null comment '用户简介',
    user_role     varchar(256) default 'user'            not null comment '用户角色:user/vip/admin',
    create_time   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted       tinyint      default 0                 not null comment '是否删除',
    PRIMARY KEY (id),
    UNIQUE INDEX unique_user_account (user_account),
    INDEX idx_user_name (user_name)
) comment '用户表';