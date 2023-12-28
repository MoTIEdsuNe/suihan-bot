package com.motiedsune.system.bots.update.command;

import com.motiedsune.system.bots.service.IBotBaseService;
import com.motiedsune.system.bots.update.IUpdateCommand;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * 用途：
 *
 * @author MoTIEdsuNe
 * @date 2023-12-21 星期四
 */
@Service
public class UpdateCommandDelete implements IUpdateCommand {

    @Resource
    IBotBaseService baseService;

    @Override
    public Set<String> commands() {
        return Collections.singleton("delete");
    }

    @Override
    public Boolean consume(Update update) {
        if(update.hasMessage()){
            Message message = update.getMessage();
            User user = message.getFrom();
            Chat chat = message.getChat();
            Message reply = message.getReplyToMessage();
            boolean flag = true;
            // 所有人删消息
            if(message.isReply() && Objects.equals(user.getId(),240564004L)){
                baseService.deleteMessage(reply.getChatId(), reply.getMessageId());
                flag = false;
            }
            // 群管理删消息
            if(flag && message.isReply() && chat.isGroupChat() && baseService.isUserAdmin(message.getChatId(), user.getId())){
                baseService.deleteMessage(reply.getChatId(), reply.getMessageId());
                flag = false;
            }

            if(flag){
                // 啥也不做
            }
        }
        return null;
    }
}
