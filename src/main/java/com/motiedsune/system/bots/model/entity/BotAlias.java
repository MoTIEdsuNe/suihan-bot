package com.motiedsune.system.bots.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用途：
 *
 * @author MoTIEdsuNe
 * @date 2023-12-14 星期四
 */

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("BOT_CHAT")
@Builder
public class BotAlias extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String origin;

    private String target;
}
