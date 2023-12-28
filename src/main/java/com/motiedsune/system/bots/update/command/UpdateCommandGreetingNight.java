package com.motiedsune.system.bots.update.command;

import com.motiedsune.system.bots.model.entity.BotGreeting;
import com.motiedsune.system.bots.service.BotGreetingService;
import com.motiedsune.system.bots.service.IBotSender;
import com.motiedsune.system.bots.update.IUpdateCommand;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

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
public class UpdateCommandGreetingNight implements IUpdateCommand {

    @Resource
    IBotSender sender;

    @Resource
    BotGreetingService greetingService;

    static Set<String> commands = new HashSet<>(Arrays.asList("night", "oyasumi", "晚安", "おやすみ"));

    @Override
    public Set<String> commands() {
        return commands;
    }

    @Override
    public Boolean  consume(Update update) {
        if (!update.hasMessage()) return null;
        Message message = update.getMessage();
        Long chatId = message.getChatId();
        User user = message.getFrom();
        // 查老的
        BotGreeting night = greetingService.lambdaQuery()
                .eq(BotGreeting::getUserId, user.getId())
                .orderByDesc(BotGreeting::getRecordTime)
                .last("LIMIT 1")
                .oneOpt().orElse(null);

        String msg = null;
        if (night != null && "morning".equals(night.getType())) {
            Duration duration = Duration.between(night.getCreateTime(), LocalDateTime.now());
            long hours = duration.toHours();
            if(hours > 1L){
                msg = String.format("%s 晚安, 做个好梦哟！今天你醒了 %s 小时哦，辛苦啦~！", message.getFrom().getFirstName(), hours);
            }else {
                msg = String.format("%s 晚安, 做个好梦哟！醒太少了不许睡，起来玩！", message.getFrom().getFirstName());
            }
        }
        if (message.getReplyToMessage() != null) {
            msg = String.format("%s 对 %s 说：晚安啦，明天见~！", message.getFrom().getFirstName(), message.getReplyToMessage().getFrom().getFirstName());
        }
        if (msg == null) {
            msg = String.format("%s 晚安, 做个好梦哟！", message.getFrom().getFirstName());
        }
        // 记录新的
        BotGreeting greeting = new BotGreeting();
        greeting.setType("night");
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
