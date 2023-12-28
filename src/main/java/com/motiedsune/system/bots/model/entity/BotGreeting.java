package com.motiedsune.system.bots.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用途： 用于记录早安和晚安
 *
 * @author MoTIEdsuNe
 * @date 2023-12-07 星期四
 */
@Data
@TableName("BOT_GREETING")
public class BotGreeting extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    // 触发用户 id
    private Long userId;

    // 早安、晚安、午安等
    private String type;

    // 记录时间
    private LocalDateTime recordTime;

    // 记录地点（触发时的 chatId）
    private Long recordId;
}
