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
create table if not exists image
(
    id             bigint                             not null auto_increment comment '图片id',
    image_name     varchar(256)                       not null comment '图片名称',
    image_intro    varchar(512)                       null comment '图片简介',
    image_url      varchar(512)                       not null comment '图片url',
    image_category varchar(64)                        null comment '图片分类',
    image_tags     varchar(512)                       null comment '图片标签(JSON)',
    image_size     bigint                             null comment '图片大小',
    image_width    int                                null comment '图片宽度',
    image_height   int                                null comment '图片高度',
    image_scale    double                             null comment '图片宽高比',
    image_format   varchar(32)                        null comment '图片格式',
    image_color    varchar(16)                        null comment '图片主色调',
    thumbnail_url  varchar(512)                       null comment '缩略图url',
    user_id        bigint                             not null comment '创建用户id',
    space_id       bigint   default 0                 not null comment '空间id,0为公共空间',
    review_status  int      default 0                 not null comment '审核状态 0:审核中,1:通过,2:驳回',
    review_message varchar(512)                       null comment '审核信息',
    reviewer_id    bigint                             null comment '审核人id',
    review_time    datetime                           null comment '审核时间',
    create_time    datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted        tinyint  default 0                 not null comment '是否删除',
    PRIMARY KEY (id),
    INDEX idx_name (image_name),
    INDEX idx_intro (image_intro),
    INDEX idx_category (image_category),
    INDEX idx_tags (image_tags),
    INDEX idx_user_id (user_id),
    INDEX idx_space_id (space_id),
    INDEX idx_review_status (review_status)
) comment '图片表';

-- 空间表
create table if not exists space
(
    id            bigint                             not null auto_increment comment '空间id',
    space_name    varchar(128)                       not null comment '空间名称',
    space_level   int      default 0                 not null comment '空间级别 0:普通版,1:专业版,2:旗舰版',
    space_type    int      default 0                 not null comment '空间类型 0:私有,1:团队',
    max_size      bigint   default 0                 not null comment '空间图片的最大容量',
    max_count     bigint   default 0                 not null comment '空间图片的最大数量',
    current_size  bigint   default 0                 not null comment '当前空间已使用容量',
    current_count bigint   default 0                 not null comment '当前空间已使用数量',
    user_id       bigint                             not null comment '创建用户id',
    create_time   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted       tinyint  default 0                 not null comment '是否删除',
    PRIMARY KEY (id),
    INDEX idx_userId (user_id),
    INDEX idx_space_name (space_name),
    INDEX idx_space_level (space_level),
    INDEX idx_space_type (space_type)
) comment '空间表';

-- 空间成员表
create table if not exists space_user
(
    id          bigint                                not null auto_increment comment '空间成员关联id',
    space_id    bigint                                not null comment '空间id',
    user_id     bigint                                not null comment '用户id',
    space_role  varchar(32) default 'viewer'          not null comment '空间角色 viewer/editor/admin',
    create_time datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted     tinyint     default 0                 not null comment '是否删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_space_id_user_id (space_id, user_id),
    INDEX idx_user_id (user_id)
) comment '空间成员表';

-- 空间角色表
create table if not exists space_role
(
    id          bigint                                not null auto_increment comment '空间角色id',
    role_key    varchar(32)                           not null comment '角色标识 viewer/editor/admin',
    role_name   varchar(64)                           not null comment '角色名称',
    role_desc   varchar(256)                          null comment '角色描述',
    create_time datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted     tinyint     default 0                 not null comment '是否删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_key (role_key)
) comment '空间角色表';

-- 空间权限表
create table if not exists space_permission
(
    id              bigint                                not null auto_increment comment '空间权限id',
    permission_key  varchar(64)                           not null comment '权限标识',
    permission_name varchar(64)                           not null comment '权限名称',
    resource        varchar(32)                           not null comment '资源',
    action          varchar(32)                           not null comment '操作',
    permission_desc varchar(256)                          null comment '权限描述',
    create_time     datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time     datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted         tinyint     default 0                 not null comment '是否删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_permission_key (permission_key),
    INDEX idx_resource (resource)
) comment '空间权限表';

-- 空间角色权限关联表
create table if not exists space_role_permission
(
    id            bigint                                not null auto_increment comment '关联id',
    role_id       bigint                                not null comment '空间角色id',
    permission_id bigint                                not null comment '空间权限id',
    create_time   datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted       tinyint     default 0                 not null comment '是否删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_id_permission_id (role_id, permission_id),
    INDEX idx_permission_id (permission_id)
) comment '空间角色权限关联表';

-- 初始化空间角色
insert into space_role (id, role_key, role_name, role_desc)
values (1, 'viewer', '浏览者', '仅可查看空间内的图片'),
       (2, 'editor', '编辑者', '可查看,上传,编辑,删除图片'),
       (3, 'admin', '管理员', '拥有空间内全部权限,包括成员管理');

-- 初始化空间权限
insert into space_permission (id, permission_key, permission_name, resource, action, permission_desc)
values (1, 'image:view', '查看图片', 'image', 'view', '查看空间内的图片'),
       (2, 'image:upload', '上传图片', 'image', 'upload', '向空间上传图片'),
       (3, 'image:edit', '编辑图片', 'image', 'edit', '编辑空间内的图片'),
       (4, 'image:delete', '删除图片', 'image', 'delete', '删除空间内的图片'),
       (5, 'spaceUser:manage', '成员管理', 'spaceUser', 'manage', '管理空间成员');

-- 初始化角色权限关联
insert into space_role_permission (role_id, permission_id)
values (1, 1),
       (2, 1),
       (2, 2),
       (2, 3),
       (2, 4),
       (3, 1),
       (3, 2),
       (3, 3),
       (3, 4),
       (3, 5);
