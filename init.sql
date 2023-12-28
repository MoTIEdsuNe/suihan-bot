# Bot 数据表
CREATE TABLE `BOT_ALIAS`
(
    `id`     bigint                                 DEFAULT NULL COMMENT 'id',
    `origin` varchar(63) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '新指令',
    `target` varchar(63) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '映射目标指令'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='命令映射表';

CREATE TABLE `BOT_CHAT`
(
    `id`               bigint NOT NULL,
    `type`             varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `title`            varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `authorize`        tinyint(1)                              DEFAULT NULL COMMENT '授权确认',
    `consent_admin_id` bigint                                  DEFAULT NULL,
    `create_time`      datetime                                DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      datetime                                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='CHAT 表';

CREATE TABLE `BOT_DO_WHAT`
(
    `id`          bigint NOT NULL AUTO_INCREMENT,
    `type`        varchar(31) COLLATE utf8mb4_unicode_ci  DEFAULT NULL COMMENT '类型',
    `name`        varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '内容',
    `stats`       int                                     DEFAULT NULL COMMENT '状态',
    `user_id`     bigint                                  DEFAULT NULL COMMENT '提交用户 id',
    `create_time` datetime                                DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime                                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 25
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='做什么';

CREATE TABLE `BOT_GREETING`
(
    `id`          bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     bigint NOT NULL COMMENT '触发用户 id，关联 BotUser 表',
    `type`        varchar(255) DEFAULT NULL COMMENT '早安、晚安、午安等',
    `record_time` datetime     DEFAULT NULL COMMENT '记录时间',
    `record_id`   bigint       DEFAULT NULL COMMENT '记录地点（触发时的 chatId）, 关联 BotChat 表',
    `create_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 56
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='问候表';

CREATE TABLE `BOT_MESSAGE`
(
    `id`                  bigint NOT NULL,
    `message_thread_id`   bigint   DEFAULT NULL,
    `from_id`             bigint   DEFAULT NULL,
    `date`                datetime DEFAULT NULL,
    `chat_id`             bigint   DEFAULT NULL,
    `text`                longtext,
    `reply_to_message_id` bigint   DEFAULT NULL,
    `data`                longtext,
    `create_time`         datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `fk_replyToMessageId_idx` (`reply_to_message_id`),
    CONSTRAINT `fk_replyToMessageId` FOREIGN KEY (`reply_to_message_id`) REFERENCES `BOT_MESSAGE` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='消息表';

CREATE TABLE `BOT_SCHEDULED`
(
    `列_name` int DEFAULT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='定时任务表';

CREATE TABLE `BOT_SLEEP`
(
    `id`           bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `chat_id`      bigint       DEFAULT NULL COMMENT '聊天ID',
    `from_user_id` bigint       DEFAULT NULL COMMENT '发送者用户ID',
    `to_user_id`   bigint       DEFAULT NULL COMMENT '接收者用户ID',
    `start_time`   datetime     DEFAULT NULL COMMENT '开始时间',
    `end_time`     datetime     DEFAULT NULL COMMENT '结束时间',
    `time_zone`    varchar(255) DEFAULT NULL COMMENT '时区',
    `cron`         varchar(255) DEFAULT NULL COMMENT 'Cron 表达式',
    `frequency`    int          DEFAULT NULL,
    `length`       int          DEFAULT NULL,
    `msg`          varchar(255) DEFAULT NULL COMMENT '消息',
    `type`         varchar(255) DEFAULT NULL COMMENT '类型',
    `status`       int          DEFAULT NULL COMMENT '状态',
    `create_time`  datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 12
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='BOT_SLEEP';

CREATE TABLE `BOT_TODO`
(
    `id`          bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     bigint NOT NULL COMMENT '用户ID',
    `chat_id`     bigint NOT NULL COMMENT '聊天ID',
    `data`        varchar(255) DEFAULT NULL COMMENT '数据',
    `status`      varchar(255) DEFAULT NULL COMMENT '状态',
    `create_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 2
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='TODO 表';

CREATE TABLE `BOT_USER`
(
    `id`            bigint NOT NULL,
    `username`      varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `first_name`    varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `language_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `create_time`   datetime                                DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime                                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `authorize`     tinyint(1)                              DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户表';


# Quartz 数据表

CREATE TABLE `QRTZ_BLOB_TRIGGERS`
(
    `SCHED_NAME`    varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
    `TRIGGER_NAME`  varchar(190) COLLATE utf8mb4_unicode_ci NOT NULL,
    `TRIGGER_GROUP` varchar(190) COLLATE utf8mb4_unicode_ci NOT NULL,
    `BLOB_DATA`     blob,
    PRIMARY KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`),
    KEY `SCHED_NAME` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`),
    CONSTRAINT `QRTZ_BLOB_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `QRTZ_CALENDARS`
(
    `SCHED_NAME`    varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
    `CALENDAR_NAME` varchar(190) COLLATE utf8mb4_unicode_ci NOT NULL,
    `CALENDAR`      blob                                    NOT NULL,
    PRIMARY KEY (`SCHED_NAME`, `CALENDAR_NAME`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `QRTZ_CRON_TRIGGERS`
(
    `SCHED_NAME`      varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
    `TRIGGER_NAME`    varchar(190) COLLATE utf8mb4_unicode_ci NOT NULL,
    `TRIGGER_GROUP`   varchar(190) COLLATE utf8mb4_unicode_ci NOT NULL,
    `CRON_EXPRESSION` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
    `TIME_ZONE_ID`    varchar(80) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    PRIMARY KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`),
    CONSTRAINT `QRTZ_CRON_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `QRTZ_FIRED_TRIGGERS`
(
    `SCHED_NAME`        varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
    `ENTRY_ID`          varchar(95) COLLATE utf8mb4_unicode_ci  NOT NULL,
    `TRIGGER_NAME`      varchar(190) COLLATE utf8mb4_unicode_ci NOT NULL,
    `TRIGGER_GROUP`     varchar(190) COLLATE utf8mb4_unicode_ci NOT NULL,
    `INSTANCE_NAME`     varchar(190) COLLATE utf8mb4_unicode_ci NOT NULL,
    `FIRED_TIME`        bigint                                  NOT NULL,
    `SCHED_TIME`        bigint                                  NOT NULL,
    `PRIORITY`          int                                     NOT NULL,
    `STATE`             varchar(16) COLLATE utf8mb4_unicode_ci  NOT NULL,
    `JOB_NAME`          varchar(190) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `JOB_GROUP`         varchar(190) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `IS_NONCONCURRENT`  varchar(1) COLLATE utf8mb4_unicode_ci   DEFAULT NULL,
    `REQUESTS_RECOVERY` varchar(1) COLLATE utf8mb4_unicode_ci   DEFAULT NULL,
    PRIMARY KEY (`SCHED_NAME`, `ENTRY_ID`),
    KEY `IDX_QRTZ_FT_TRIG_INST_NAME` (`SCHED_NAME`, `INSTANCE_NAME`),
    KEY `IDX_QRTZ_FT_INST_JOB_REQ_RCVRY` (`SCHED_NAME`, `INSTANCE_NAME`, `REQUESTS_RECOVERY`),
    KEY `IDX_QRTZ_FT_J_G` (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`),
    KEY `IDX_QRTZ_FT_JG` (`SCHED_NAME`, `JOB_GROUP`),
    KEY `IDX_QRTZ_FT_T_G` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`),
    KEY `IDX_QRTZ_FT_TG` (`SCHED_NAME`, `TRIGGER_GROUP`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `QRTZ_JOB_DETAILS`
(
    `SCHED_NAME`        varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
    `JOB_NAME`          varchar(190) COLLATE utf8mb4_unicode_ci NOT NULL,
    `JOB_GROUP`         varchar(190) COLLATE utf8mb4_unicode_ci NOT NULL,
    `DESCRIPTION`       varchar(250) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `JOB_CLASS_NAME`    varchar(250) COLLATE utf8mb4_unicode_ci NOT NULL,
    `IS_DURABLE`        varchar(1) COLLATE utf8mb4_unicode_ci   NOT NULL,
    `IS_NONCONCURRENT`  varchar(1) COLLATE utf8mb4_unicode_ci   NOT NULL,
    `IS_UPDATE_DATA`    varchar(1) COLLATE utf8mb4_unicode_ci   NOT NULL,
    `REQUESTS_RECOVERY` varchar(1) COLLATE utf8mb4_unicode_ci   NOT NULL,
    `JOB_DATA`          blob,
    PRIMARY KEY (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`),
    KEY `IDX_QRTZ_J_REQ_RECOVERY` (`SCHED_NAME`, `REQUESTS_RECOVERY`),
    KEY `IDX_QRTZ_J_GRP` (`SCHED_NAME`, `JOB_GROUP`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `QRTZ_LOCKS`
(
    `SCHED_NAME` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
    `LOCK_NAME`  varchar(40) COLLATE utf8mb4_unicode_ci  NOT NULL,
    PRIMARY KEY (`SCHED_NAME`, `LOCK_NAME`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `QRTZ_PAUSED_TRIGGER_GRPS`
(
    `SCHED_NAME`    varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
    `TRIGGER_GROUP` varchar(190) COLLATE utf8mb4_unicode_ci NOT NULL,
    PRIMARY KEY (`SCHED_NAME`, `TRIGGER_GROUP`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `QRTZ_SCHEDULER_STATE`
(
    `SCHED_NAME`        varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
    `INSTANCE_NAME`     varchar(190) COLLATE utf8mb4_unicode_ci NOT NULL,
    `LAST_CHECKIN_TIME` bigint                                  NOT NULL,
    `CHECKIN_INTERVAL`  bigint                                  NOT NULL,
    PRIMARY KEY (`SCHED_NAME`, `INSTANCE_NAME`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `QRTZ_SIMPLE_TRIGGERS`
(
    `SCHED_NAME`      varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
    `TRIGGER_NAME`    varchar(190) COLLATE utf8mb4_unicode_ci NOT NULL,
    `TRIGGER_GROUP`   varchar(190) COLLATE utf8mb4_unicode_ci NOT NULL,
    `REPEAT_COUNT`    bigint                                  NOT NULL,
    `REPEAT_INTERVAL` bigint                                  NOT NULL,
    `TIMES_TRIGGERED` bigint                                  NOT NULL,
    PRIMARY KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`),
    CONSTRAINT `QRTZ_SIMPLE_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `QRTZ_SIMPROP_TRIGGERS`
(
    `SCHED_NAME`    varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
    `TRIGGER_NAME`  varchar(190) COLLATE utf8mb4_unicode_ci NOT NULL,
    `TRIGGER_GROUP` varchar(190) COLLATE utf8mb4_unicode_ci NOT NULL,
    `STR_PROP_1`    varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `STR_PROP_2`    varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `STR_PROP_3`    varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `INT_PROP_1`    int                                     DEFAULT NULL,
    `INT_PROP_2`    int                                     DEFAULT NULL,
    `LONG_PROP_1`   bigint                                  DEFAULT NULL,
    `LONG_PROP_2`   bigint                                  DEFAULT NULL,
    `DEC_PROP_1`    decimal(13, 4)                          DEFAULT NULL,
    `DEC_PROP_2`    decimal(13, 4)                          DEFAULT NULL,
    `BOOL_PROP_1`   varchar(1) COLLATE utf8mb4_unicode_ci   DEFAULT NULL,
    `BOOL_PROP_2`   varchar(1) COLLATE utf8mb4_unicode_ci   DEFAULT NULL,
    PRIMARY KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`),
    CONSTRAINT `QRTZ_SIMPROP_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `QRTZ_TRIGGERS`
(
    `SCHED_NAME`     varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
    `TRIGGER_NAME`   varchar(190) COLLATE utf8mb4_unicode_ci NOT NULL,
    `TRIGGER_GROUP`  varchar(190) COLLATE utf8mb4_unicode_ci NOT NULL,
    `JOB_NAME`       varchar(190) COLLATE utf8mb4_unicode_ci NOT NULL,
    `JOB_GROUP`      varchar(190) COLLATE utf8mb4_unicode_ci NOT NULL,
    `DESCRIPTION`    varchar(250) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `NEXT_FIRE_TIME` bigint                                  DEFAULT NULL,
    `PREV_FIRE_TIME` bigint                                  DEFAULT NULL,
    `PRIORITY`       int                                     DEFAULT NULL,
    `TRIGGER_STATE`  varchar(16) COLLATE utf8mb4_unicode_ci  NOT NULL,
    `TRIGGER_TYPE`   varchar(8) COLLATE utf8mb4_unicode_ci   NOT NULL,
    `START_TIME`     bigint                                  NOT NULL,
    `END_TIME`       bigint                                  DEFAULT NULL,
    `CALENDAR_NAME`  varchar(190) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `MISFIRE_INSTR`  smallint                                DEFAULT NULL,
    `JOB_DATA`       blob,
    PRIMARY KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`),
    KEY `IDX_QRTZ_T_J` (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`),
    KEY `IDX_QRTZ_T_JG` (`SCHED_NAME`, `JOB_GROUP`),
    KEY `IDX_QRTZ_T_C` (`SCHED_NAME`, `CALENDAR_NAME`),
    KEY `IDX_QRTZ_T_G` (`SCHED_NAME`, `TRIGGER_GROUP`),
    KEY `IDX_QRTZ_T_STATE` (`SCHED_NAME`, `TRIGGER_STATE`),
    KEY `IDX_QRTZ_T_N_STATE` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`, `TRIGGER_STATE`),
    KEY `IDX_QRTZ_T_N_G_STATE` (`SCHED_NAME`, `TRIGGER_GROUP`, `TRIGGER_STATE`),
    KEY `IDX_QRTZ_T_NEXT_FIRE_TIME` (`SCHED_NAME`, `NEXT_FIRE_TIME`),
    KEY `IDX_QRTZ_T_NFT_ST` (`SCHED_NAME`, `TRIGGER_STATE`, `NEXT_FIRE_TIME`),
    KEY `IDX_QRTZ_T_NFT_MISFIRE` (`SCHED_NAME`, `MISFIRE_INSTR`, `NEXT_FIRE_TIME`),
    KEY `IDX_QRTZ_T_NFT_ST_MISFIRE` (`SCHED_NAME`, `MISFIRE_INSTR`, `NEXT_FIRE_TIME`, `TRIGGER_STATE`),
    KEY `IDX_QRTZ_T_NFT_ST_MISFIRE_GRP` (`SCHED_NAME`, `MISFIRE_INSTR`, `NEXT_FIRE_TIME`, `TRIGGER_GROUP`,
                                         `TRIGGER_STATE`),
    CONSTRAINT `QRTZ_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`) REFERENCES `QRTZ_JOB_DETAILS` (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
