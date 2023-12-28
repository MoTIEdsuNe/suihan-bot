package com.motiedsune.system.bots.update.command;

import com.motiedsune.system.bots.model.entity.BotTodo;
import com.motiedsune.system.bots.model.enums.Status;
import com.motiedsune.system.bots.service.BotTodoService;
import com.motiedsune.system.bots.service.IBotBaseService;
import com.motiedsune.system.bots.service.IBotSender;
import com.motiedsune.system.bots.update.IUpdateCommand;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

/**
 * 用途：
 *
 * @author MoTIEdsuNe
 * @date 2023-12-18 星期一
 */
@Service
public class UpdateCommandTodo {

    @Resource
    private BotTodoService todoService;

    @Resource
    private IBotBaseService botBaseService;

    @Resource
    private IBotSender sender;

    @Service
    class UpdateAddTodo implements IUpdateCommand {
        @Override
        public Set<String> commands() {
            return Collections.singleton("todo-add");
        }

        @Override
        public Boolean consume(Update update) {
            if (!update.hasMessage()) return Boolean.FALSE;
            Message message = update.getMessage();
            User user = message.getFrom();
            Chat chat = message.getChat();
            Long chatId = message.getChatId();
            Long userId = user.getId();
            String text = message.getText();
            String[] texts = text.split(" ");
            if (texts.length < 2) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chatId);
                sendMessage.setText("指令缺失，该指令需要至少 2 条信息！");
                sendMessage.setReplyToMessageId(message.getMessageId());
                Message senderMsg = sender.sender(sendMessage);
                botBaseService.deleteMessage(chatId,senderMsg.getMessageId(),5);
                return Boolean.FALSE;
            }
            String data = String.join(" ", Arrays.copyOfRange(texts, 1, texts.length));
            BotTodo todo = BotTodo.builder().chatId(chatId).userId(userId).data(data).status(Status.ENABLE).build();
            todoService.save(todo);
            return Boolean.TRUE;
        }
    }

    @Service
    class UpdateDelTodo implements IUpdateCommand {

        @Override
        public Set<String> commands() {
            return Collections.singleton("todo-del");
        }

        @Override
        public Boolean consume(Update update) {
            return null;
        }
    }

    @Service
    class UpdateListTodo implements IUpdateCommand {

        @Override
        public Set<String> commands() {
            return Collections.singleton("todolist");
        }

        @Override
        public Boolean consume(Update update) {
            return null;
        }
    }
}
