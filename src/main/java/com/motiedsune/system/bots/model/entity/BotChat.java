package com.motiedsune.system.bots.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.telegram.telegrambots.meta.api.objects.Chat;

/**
 * 用途：
 *
 * @author MoTIEdsuNe
 * @date 2023-12-07 星期四
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("BOT_CHAT")
public class BotChat extends BaseEntity {

    @TableId
    private Long id;

    private String type;

    private String title;

    // 同意在某群使用的管理员 id (需要该管理员首先同意使用本 bot）
    private Long consentAdminId;

    private Boolean authorize;

    public BotChat() {}

    public BotChat(Chat chat, Long userId) {
        this.id = chat.getId();
        this.title = chat.getTitle();
        this.type = chat.getType();
        this.authorize = true;
        this.consentAdminId = userId;
    }
}
