package com.motiedsune.system.bots;

import com.motiedsune.system.bots.botlist.BaseBot;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.Webhook;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultWebhook;

/**
 * 用途：
 *
 * @author MoTIEdsuNe
 * @date 2023-12-07 星期四
 */
@Slf4j
@Component
public class StartBots {


    @Resource
    BaseBot baseBot;

    @Resource
    TelegramBotsApi telegramBotsApi;

    @PostConstruct
    public void init() {
        start();
    }

    public void start() {
        log.info("【系统启动】Telegram bot start running!");
        try {
            // 所有需要启动的 Bot 都从这里注册
            telegramBotsApi.registerBot(baseBot);

        } catch (TelegramApiException e) {
            log.error("【系统异常】Telegram bot module The run failed");
            log.error("【系统异常】Telegram bot", e);
        }

    }
}
