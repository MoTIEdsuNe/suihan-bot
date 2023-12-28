package com.motiedsune.system.bots.service;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.io.Serializable;

/**
 * 用途：
 *
 * @author MoTIEdsuNe
 * @date 2023-12-12 星期二
 */
public interface IBotSender {

    AbsSender sender();

    <T extends Serializable, Method extends BotApiMethod<T>> T sender(Method method);

    <T extends Serializable, Method extends BotApiMethod<T>> T sender(Method method, String msg);
}
