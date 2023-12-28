package com.motiedsune.system.bots.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用途：
 *
 * @author MoTIEdsuNe
 * @date 2023-12-21 星期四
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@TableName("BOT_DO_WHAT")
public class BotDoWhat extends BaseEntity {

    @TableId
    private Long id;

    private String type;

    private String name;

    private Integer stats;

    private Long userId;
}
