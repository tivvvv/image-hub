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
    user_role     varchar(256) default 'user'            not null comment '用户角色:user/vip/banned/admin',
    create_time   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted       tinyint      default 0                 not null comment '是否删除',
    PRIMARY KEY (id),
    UNIQUE INDEX unique_user_account (user_account),
    INDEX idx_user_name (user_name)
) comment '用户表';

-- 图片表
create table if not exists picture
(
    id             bigint                             not null auto_increment comment '图片id',
    pic_name       varchar(256)                       not null comment '图片名称',
    pic_intro      varchar(512)                       null comment '图片简介',
    pic_url        varchar(512)                       not null comment '图片url',
    pic_category   varchar(64)                        null comment '图片分类',
    pic_tags       varchar(512)                       null comment '图片标签(JSON)',
    pic_size       bigint                             null comment '图片大小',
    pic_width      int                                null comment '图片宽度',
    pic_height     int                                null comment '图片高度',
    pic_scale      double                             null comment '图片宽高比',
    pic_format     varchar(32)                        null comment '图片格式',
    user_id        bigint                             not null comment '创建用户id',
    review_status  int      default 0                 not null comment '审核状态 0:审核中,1:通过,2:驳回',
    review_message varchar(512)                       null comment '审核信息',
    reviewer_id    bigint                             null comment '审核人id',
    review_time    datetime                           null comment '审核时间',
    create_time    datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted        tinyint  default 0                 not null comment '是否删除',
    PRIMARY KEY (id),
    INDEX idx_name (pic_name),
    INDEX idx_intro (pic_intro),
    INDEX idx_category (pic_category),
    INDEX idx_tags (pic_tags),
    INDEX idx_user_id (user_id),
    INDEX idx_review_status (review_status)
) comment '图片表';
