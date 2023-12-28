package com.motiedsune.system.bots.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.LocalDateTime;

/**
 * 用途：
 *
 * @author MoTIEdsuNe
 * @date 2023-12-07 星期四
 */

@Data
@TableName("BOT_USER")
public class BotUser extends BaseEntity {

    @TableId
    private Long id;

    private String username;

    private String firstName;

    private String languageCode;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    //true :同意 false：不存在
    private Boolean authorize;

    public BotUser() {}

    public BotUser(Long id, String username, String firstName, String languageCode, Boolean authorize) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.languageCode = languageCode;
        this.authorize = authorize;
    }

    public BotUser(User user) {
        this.id = user.getId();
        this.username = user.getUserName();
        this.firstName = user.getFirstName();
        this.languageCode = user.getLanguageCode();
        this.authorize = true;
    }
}
