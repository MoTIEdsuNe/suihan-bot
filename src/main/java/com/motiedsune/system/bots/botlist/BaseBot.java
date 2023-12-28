package com.motiedsune.system.bots.botlist;

import com.motiedsune.system.bots.service.IBotSender;
import com.motiedsune.system.bots.update.UpdateManager;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;
import java.util.List;

/**
 * 用途：BaseBot 最基本的 Bot 
 *
 * @author MoTIEdsuNe
 * @date 2023-12-01 星期五
 */
@Slf4j
@Component
public class BaseBot extends TelegramLongPollingBot implements IBotSender {

    @Value("${bot.telegram.base.name}")
    private String BOT_NAME;

    @Resource
    @Lazy
    private UpdateManager manager;

    public BaseBot(DefaultBotOptions options, Environment env) {
        super(options, env.getProperty("bot.telegram.base.token"));
    }

    @Override
    public void onUpdateReceived(Update update) {
        manager.consume(update);
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        super.onUpdatesReceived(updates);
    }

    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }

    @Override
    public void onRegister() {
        super.onRegister();
    }

    @Override
    public AbsSender sender() {
        return this;
    }

    @Override
    public <Result extends Serializable, Method extends BotApiMethod<Result>> Result sender(Method method) {
        try {
            return super.execute(method);
        } catch (TelegramApiException e) {
            log.error("execute error", e);
        }
        return null;
    }


    @Override
    public <Result extends Serializable, Method extends BotApiMethod<Result>> Result sender(Method method, String msg) {
        try {
            return super.execute(method);
        } catch (TelegramApiException e) {
            log.error("execute error", e);
        }
        return null;
    }
}
