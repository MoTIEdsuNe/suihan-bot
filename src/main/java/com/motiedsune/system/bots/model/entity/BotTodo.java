package com.motiedsune.system.bots.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.motiedsune.system.bots.model.enums.Status;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用途：
 *
 * @author MoTIEdsuNe
 * @date 2023-12-18 星期一
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@TableName("BOT_TODO")
public class BotTodo extends BaseEntity {

    @TableId
    private Long id;

    private Long userId;

    private Long chatId;

    // 数据
    private String data;

    // 是否完成
    private Status status;
}
