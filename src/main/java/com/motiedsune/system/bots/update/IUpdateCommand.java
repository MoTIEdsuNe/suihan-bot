package com.motiedsune.system.bots.update;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.Serializable;
import java.util.Set;

/**
 * 用途：
 *
 * @author MoTIEdsuNe
 * @date 2023-12-08 星期五
 */
public interface IUpdateCommand {

    /**
     * 获取命令列表
     */
    Set<String> commands();

    /**
     * 调用命令
     */
    Boolean consume(Update update);


    default Boolean callbackQuery(Update update) {
        return null;
    }

    ;
}
