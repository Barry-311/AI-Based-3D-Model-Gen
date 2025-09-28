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

CREATE TABLE `model_3d` (
                            `id`               bigint                                  NOT NULL AUTO_INCREMENT COMMENT 'id',
                            `taskId`           varchar(256)                            NOT NULL COMMENT 'Tripo3D任务ID',
                            `userId`           bigint                                  NULL COMMENT '用户ID',
                            `name`             varchar(256)                            NOT NULL COMMENT '模型名称',
                            `prompt`           text                                    NULL COMMENT '生成提示词',
                            `requestSignature` varchar(128)                            NULL COMMENT '请求签名',
                            `status`           varchar(50)                             NOT NULL COMMENT '任务状态',
                            `progress`         int           DEFAULT 0                 NOT NULL COMMENT '生成进度',
                            `pbrModelUrl`      varchar(1024)                           NULL COMMENT 'PBR模型URL',
                            `renderedImageUrl` varchar(1024)                           NULL COMMENT '渲染图片URL',
                            `pictureUrl`       varchar(512)                            NULL COMMENT '用户上传图片链接',
                            `createTime`       datetime      DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
                            `updateTime`       datetime      DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            `isPublic`         tinyint(1)    DEFAULT 0                 NOT NULL COMMENT '是否公开',
                            `isDelete`         tinyint       DEFAULT 0                 NOT NULL COMMENT '是否删除',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_taskId` (`taskId`),
                            INDEX `idx_userId` (`userId`),
                            INDEX `idx_status` (`status`),
                            INDEX `idx_name` (`name`(191)), -- 对长 varchar 字段创建索引时建议指定前缀长度
                            CONSTRAINT `fk_model3d_user` FOREIGN KEY (`userId`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='3D模型';


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

ALTER TABLE model_3d
    -- 添加新列
    ADD COLUMN genTime VARCHAR(50) NULL COMMENT '生成时间';

ALTER TABLE model_3d
    -- 添加请求签名+用户唯一索引，避免重复记录
    ADD UNIQUE KEY `uk_requestSignature_userId` (`requestSignature`, `userId`);

ALTER TABLE model_3d
    -- 添加请求签名索引
    ADD INDEX idx_requestSignature (requestSignature);