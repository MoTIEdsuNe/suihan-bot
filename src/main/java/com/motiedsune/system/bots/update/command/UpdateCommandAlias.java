package com.motiedsune.system.bots.update.command;

import com.motiedsune.system.bots.model.entity.BotAlias;
import com.motiedsune.system.bots.service.BotAliasService;
import com.motiedsune.system.bots.service.IBotSender;
import com.motiedsune.system.bots.update.IUpdateCommand;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用途：
 *
 * @author MoTIEdsuNe
 * @date 2023-12-14 星期四
 */

@Service
public class UpdateCommandAlias implements IUpdateCommand {

    @Resource
    IBotSender sender;

    @Resource
    BotAliasService aliasService;

    @Override
    public Set<String> commands() {
        return new HashSet<>(List.of("alias"));
    }

    @Override
    public Boolean consume(Update update) {
        if (!update.hasMessage()) return null;
        Message message = update.getMessage();
        Long chatId = message.getChatId();
        String text = message.getText();

        String[] split = text.split(" ");
        if (split.length < 2) {
            SendMessage result = new SendMessage();
            result.setChatId(chatId);
            result.setText("你想做点什么？\n 格式：\n-> /alise [origin] [target]");
            sender.sender(result);
            return false;
        }

        this.aliasService.save(BotAlias.builder()
                .origin(split[1])
                .target(split[2])
                .build());

        SendMessage result = new SendMessage();
        result.setChatId(chatId);
        result.setText(String.format("已完成：%s -> %s",split[1],split[2]));
        sender.sender(result);
        return Boolean.TRUE;
    }
}
