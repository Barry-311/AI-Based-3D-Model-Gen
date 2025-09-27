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
    name           varchar(256)                       not null comment '模型名称',
    prompt          text                               not null comment '生成提示词',
    status          varchar(50)                        not null comment '任务状态',
    progress        int          default 0             not null comment '生成进度',
    pbrModelUrl   varchar(1024)                      null comment 'PBR模型URL',
    renderedImageUrl varchar(1024)                   null comment '渲染图片URL',
    createTime     datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime     datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete       tinyint      default 0             not null comment '是否删除',
    UNIQUE KEY uk_taskId (taskId),
    INDEX idx_status (status)
) comment '3D模型' collate = utf8mb4_unicode_ci;


ALTER TABLE model_3d
    -- 添加新列
    ADD COLUMN pictureUrl VARCHAR(512) NULL COMMENT '用户上传图片链接';

ALTER TABLE model_3d
    -- 添加新列
    ADD INDEX idx_name (name);

-- 添加userId字段绑定用户
ALTER TABLE model_3d
    ADD COLUMN userId BIGINT NULL COMMENT '用户ID',
    ADD INDEX idx_userId (userId),
    ADD CONSTRAINT fk_model3d_user FOREIGN KEY (userId) REFERENCES user(id) ON DELETE SET NULL;

ALTER TABLE model_3d
    -- 添加新列
    ADD COLUMN isPublic BOOLEAN DEFAULT FALSE NOT NULL COMMENT '是否公开';

-- 用户反馈表
create table if not exists user_feedback
(
    id              bigint auto_increment comment 'id' primary key,
    userId          bigint                             not null comment '用户ID',
    feedbackType    varchar(50)                        not null comment '反馈类型：model_quality(模型质量), user_experience(用户体验), feature_request(功能建议), bug_report(问题反馈)',
    rating          int                                null comment '评分(1-5分)',
    title           varchar(256)                       null comment '反馈标题',
    content         text                               not null comment '反馈内容',
    createTime      datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime      datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete        tinyint      default 0             not null comment '是否删除',
    INDEX idx_userId (userId),
    INDEX idx_feedbackType (feedbackType)
) comment '用户反馈表' collate = utf8mb4_unicode_ci;


