# 数据库初始化
-- 创建库
create database if not exists qiniuyun;

-- 切换库
use qiniuyun;


-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_userName (userName)
    ) comment '用户' collate = utf8mb4_unicode_ci;


-- 3D模型表
create table if not exists model_3d
(
    id              bigint auto_increment comment 'id' primary key,
    taskId         varchar(256)                       not null comment 'Tripo3D任务ID',
    prompt          text                               not null comment '生成提示词',
    status          varchar(50)                        not null comment '任务状态',
    progress        int          default 0             not null comment '生成进度',
    modelUrl       varchar(1024)                      null comment '模型文件URL',
    baseModelUrl  varchar(1024)                      null comment '基础模型URL',
    pbrModelUrl   varchar(1024)                      null comment 'PBR模型URL',
    renderedImageUrl varchar(1024)                   null comment '渲染图片URL',
    localModelPath varchar(512)                      null comment '本地模型文件路径',
    fileSize       bigint       default 0             not null comment '文件大小(字节)',
    createTime     datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime     datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete       tinyint      default 0             not null comment '是否删除',
    UNIQUE KEY uk_taskId (taskId),
    INDEX idx_status (status),
    INDEX idx_createTime (createTime)
) comment '3D模型' collate = utf8mb4_unicode_ci;


ALTER TABLE model_3d
    -- 添加新列
    ADD COLUMN pictureUrl VARCHAR(512) NULL COMMENT '用户上传图片链接';