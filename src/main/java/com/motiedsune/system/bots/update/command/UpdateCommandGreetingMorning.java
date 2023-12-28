package com.motiedsune.system.bots.update.command;

import com.motiedsune.system.bots.model.entity.BotGreeting;
import com.motiedsune.system.bots.service.BotGreetingService;
import com.motiedsune.system.bots.service.IBotSender;
import com.motiedsune.system.bots.update.IUpdateCommand;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 用途：
 *
 * @author MoTIEdsuNe
 * @date 2023-12-12 星期二
 */
@Service
public class UpdateCommandGreetingMorning implements IUpdateCommand {

    @Resource
    IBotSender sender;

    @Resource
    BotGreetingService greetingService;

    static Set<String> commands = new HashSet<>(Arrays.asList("morning", "ohayo", "早安", "おはよう"));

    @Override
    public Set<String> commands() {
        return commands;
    }

    @Override
    public Boolean consume(Update update) {
        if (!update.hasMessage()) return null;
        Message message = update.getMessage();
        Long chatId = message.getChatId();
        User user = message.getFrom();
        // 查老的
        BotGreeting morning = greetingService.lambdaQuery()
                .eq(BotGreeting::getUserId, user.getId())
                .orderByDesc(BotGreeting::getRecordTime)
                .last("LIMIT 1")
                .oneOpt().orElse(null);

        String msg = null;
        if (morning != null && "night".equals(morning.getType())) {
            Duration duration = Duration.between(morning.getRecordTime(), LocalDateTime.now());
            long hours = duration.toHours();
            msg = String.format(" %s ，早上好呀！今天你睡了 %s 小时哦！", message.getFrom().getFirstName(), hours);
        }
        if (message.getReplyToMessage() != null) {
            msg = String.format("%s 对 %s 说 ~ 早安呀！", message.getFrom().getFirstName(), message.getReplyToMessage().getFrom().getFirstName());
        }
        if (msg == null) {
            msg = String.format(" %s ，早上好呀！", message.getFrom().getFirstName());
        }
        // 记录新的
        BotGreeting greeting = new BotGreeting();
        greeting.setType("morning");
        greeting.setRecordId(message.getChatId());
        greeting.setRecordTime(LocalDateTime.now());
        greeting.setUserId(user.getId());
        greetingService.save(greeting);
        SendMessage result = new SendMessage();
        result.setChatId(chatId);
        result.setText(msg);
        sender.sender(result);
        return true;
    }
}
