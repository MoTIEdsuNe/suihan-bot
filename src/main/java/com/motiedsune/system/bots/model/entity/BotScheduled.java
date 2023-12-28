package com.motiedsune.system.bots.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用途：定时提醒任务
 *
 * @author MoTIEdsuNe
 * @date 2023-12-07 星期四
 */

@Data
@TableName("BOT_SCHEDULED")
public class BotScheduled extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    // 创建定时任务的用户
    private Long userId;

    // 目标用户 id
    private Long targetUserId;

    // 创建定时任务的 chat
    private Long chatId;

    // 状态
    private String status;

    // 类型
    private String type;

    // 时长
    private String duration;

    // 消息
    private String massage;

    //

}
