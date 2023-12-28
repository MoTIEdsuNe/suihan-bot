package com.motiedsune.system.bots.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 用途：
 *
 * @author MoTIEdsuNe
 * @date 2023-12-07 星期四
 */
@Data
@TableName("BOT_MESSAGE")
public class BotMessage extends BaseEntity {
    @TableId
    private Long id;

    private Long messageThreadId;

    private Long fromId;

    private Date date;

    private Long chatId;

    private String text;

    private Long replyToMessageId;

    private String data;
}
