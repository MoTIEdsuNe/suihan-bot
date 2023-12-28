package com.motiedsune.system.bots.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
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
 * @author Xander Lau
 * @date 2023-12-05 星期二
 */
@Component
@Slf4j
public class BotConfig {

    @Value("${bot.telegram.proxy.enable}")
    private Boolean PROXY_OPEN;

    @Value("${bot.telegram.proxy.host}")
    private String PROXY_HOST;

    @Value("${bot.telegram.proxy.port}")
    private Integer PROXY_PORT;

    @Value("${bot.telegram.proxy.type}")
    private String PROXY_TYPE_STR;

    @Value("${bot.telegram.webhook.enable}")
    private Boolean WEBHOOK_OPEN;

    @Value("${bot.telegram.webhook.url:https://example.com}")
    private String WEBHOOK_URL;

    // 以下均为 Bean 提供商

    @Bean
    public DefaultBotOptions defaultBotOptions() {
        // 配置是否使用代理
        DefaultBotOptions options = new DefaultBotOptions();
        if (PROXY_OPEN) {
            log.info("【系统代理】系统代理 {}:{} 已启用!",PROXY_HOST,PROXY_PORT);
            options.setProxyHost(PROXY_HOST);
            options.setProxyPort(PROXY_PORT);
            DefaultBotOptions.ProxyType proxyType = DefaultBotOptions.ProxyType.valueOf(PROXY_TYPE_STR);
            options.setProxyType(proxyType);
        } else {
            log.info("【系统代理】系统代理未启用!");
        }
        return options;
    }

    @Bean
    public DefaultBotSession defaultBotSession(DefaultBotOptions defaultBotOptions) {
        DefaultBotSession session = new DefaultBotSession();
        session.setOptions(defaultBotOptions);
        return session;
    }

    @Bean
    @Primary
    public TelegramBotsApi telegramBotsApi(DefaultBotSession defaultBotSession) throws TelegramApiException {
        // 配置是否使用 webhook
        if (WEBHOOK_OPEN && !WEBHOOK_URL.equals("https://example.com")) {
            Webhook webhook = new DefaultWebhook();
            webhook.setInternalUrl(WEBHOOK_URL);
            log.info("【通讯类型】Webhook 已启用，URL：{}",WEBHOOK_URL);
            return new TelegramBotsApi(defaultBotSession.getClass(), webhook);
        } else {
            log.info("【通讯类型】LongPolling 已启用");
            return new TelegramBotsApi(defaultBotSession.getClass());
        }
    }
}
