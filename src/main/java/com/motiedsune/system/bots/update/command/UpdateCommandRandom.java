package com.motiedsune.system.bots.update.command;

import com.motiedsune.system.bots.update.IUpdateCommand;
import com.motiedsune.system.bots.utils.BotUtils;
import com.motiedsune.system.bots.service.IBotSender;
import jakarta.annotation.Resource;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Collections;
import java.util.Random;
import java.util.Set;

/**
 * 用途：
 *
 * @author MoTIEdsuNe
 * @date 2023-12-21 星期四
 */
@Service
public class UpdateCommandRandom implements IUpdateCommand {

    @Resource
    IBotSender sender;

    @Override
    public Set<String> commands() {
        return Collections.singleton("random");
    }

    @Override
    public Boolean consume(Update update) {
        if (!update.hasMessage()) return null;
        Message message = update.getMessage();
        Long chatId = message.getChatId();
        String msg = message.getText();

        SendMessage sendMessage = new SendMessage();
        StringBuilder text = new StringBuilder();
        String[] strings = msg.split("[ \n]");
        if (strings.length == 0) {
            text = new StringBuilder("发生了啥？");
        }

        String command = BotUtils.getCommand(strings[0]);
        Random random = new Random();
        boolean flag = true;

        // 就一个参数
        if (flag && strings.length == 1 && Strings.isNotBlank(command)) {
            text = new StringBuilder().append(random.nextInt(0, 100));
            flag = false;
        }

        if (flag && strings.length == 2 && isNumeric(strings[1])) {
            text = new StringBuilder().append(random.nextInt(0, Integer.parseInt(strings[1])));
            flag = false;
        }

        // 有俩以上参数
        if (flag && strings.length == 3 && isNumeric(strings[1]) && isNumeric(strings[2])) {
            text = new StringBuilder().append(random.nextInt(Integer.parseInt(strings[1]), Integer.parseInt(strings[2])));
            flag = false;
        }

        if (flag && strings.length > 2) {
            int nextInt = random.nextInt(2, strings.length);
            text = new StringBuilder(strings[nextInt]);
            flag = false;
        }

        if (flag) {
            text = new StringBuilder().append(random.nextInt(0, 100));
        }

        sendMessage.setChatId(chatId);
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.enableMarkdown(true);
        sendMessage.setParseMode("MarkdownV2");
        sendMessage.setText(text.toString().replace("-", "\\-"));
        if (!"".contentEquals(text)) {
            Message sender = this.sender.sender(sendMessage);
        }
        return Boolean.TRUE;
    }

    public boolean isNumeric(String str) {
        if (Strings.isBlank(str)) return false;
        boolean matches = str.matches("-?\\d+");
        return matches;
    }
}
