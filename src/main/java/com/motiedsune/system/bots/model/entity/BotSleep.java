package com.motiedsune.system.bots.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.motiedsune.system.bots.model.enums.Status;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用途：
 *
 * @author MoTIEdsuNe
 * @date 2023-12-25 星期一
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("BOT_SLEEP")
@Builder
public class BotSleep extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long chatId;

    private Long fromUserId;

    private Long toUserId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String timeZone;

    // 提醒间隔长度（分钟）
    private Integer length;

    // 提醒次数
    private Integer frequency;

    private String cron;

    private String msg;

    private String type;

    private Status status;
}
