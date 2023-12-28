package com.motiedsune.system.bots.update.command;

import com.motiedsune.system.bots.service.IBotSender;
import com.motiedsune.system.bots.update.IUpdateCommand;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Collections;
import java.util.Set;

/**
 * 用途：
 *
 * @author MoTIEdsuNe
 * @date 2023-12-21 星期四
 */
@Service
public class UpdateCommandStart implements IUpdateCommand {

    @Resource
    private IBotSender sender;

    @Override
    public Set<String> commands() {
        return Collections.singleton("start");
    }

    @Override
    public Boolean consume(Update update) {
        if (!update.hasMessage()) return null;
        Message message = update.getMessage();
        Long chatId = message.getChatId();
        User user = message.getFrom();

        String text = user.getFirstName() + "你好呀！";

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText(text);
        sender.sender(sendMessage);
        return Boolean.TRUE;
    }
}
