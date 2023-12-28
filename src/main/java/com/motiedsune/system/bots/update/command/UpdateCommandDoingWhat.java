package com.motiedsune.system.bots.update.command;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.motiedsune.system.bots.model.entity.BotDoWhat;
import com.motiedsune.system.bots.service.BotDoWhatService;
import com.motiedsune.system.bots.service.IBotBaseService;
import com.motiedsune.system.bots.service.IBotSender;
import com.motiedsune.system.bots.update.IUpdateCommand;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 用途：
 *
 * @author MoTIEdsuNe
 * @date 2023-12-21 星期四
 */
@Service
public class UpdateCommandDoingWhat implements IUpdateCommand {

    @Resource
    private IBotSender sender;

    @Resource
    private BotDoWhatService whatService;

    @Resource
    private IBotBaseService baseService;

    @Override
    public Set<String> commands() {
        return new HashSet<>(Arrays.asList("csm", "hsm", "wsm", "ksm"));
    }

    @Override
    public Boolean consume(Update update) {
        if (!update.hasMessage()) return null;
        Message message = update.getMessage();
        Long chatId = message.getChatId();
        User user = message.getFrom();
        String msg = message.getText();

        SendMessage sendMessage = new SendMessage();
        boolean buttonBool = false;
        StringBuilder text = null;
        String[] strings = msg.split("[ \n]");
        if (strings.length == 0) {
            text = new StringBuilder("发生了啥？");
        }

        String command = getCommand(strings[0]);

        // 就一个参数
        if (strings.length == 1 && Strings.isNotBlank(command)) {
            text = new StringBuilder(getDoingList(command, "1"));
            buttonBool = true;
        }

        // 有俩参数
        if (strings.length == 2 && Strings.isNotBlank(command)) {
            String number;
            if (StringUtils.equals(strings[1], "list")) {
                number = "10";
           } else {
                StringBuilder sb = new StringBuilder();
                for (char c : strings[1].toCharArray()) {
                    if (Character.isDigit(c)) {
                        sb.append(c);
                    }
                }
                number = sb.toString();
            }
            if (Strings.isBlank(number)) number = "1";
            text = new StringBuilder(getDoingList(command, number));
            buttonBool = true;
        }

        // 三个参数的时候
        boolean add = strings.length > 1 && (StringUtils.equals(strings[1], "add")
                || StringUtils.equals(strings[1], "insert")
                || StringUtils.equals(strings[1], "添加")
                || StringUtils.equals(strings[1], "插入"));

        boolean del = strings.length > 1 && (StringUtils.equals(strings[1], "del")
                || StringUtils.equals(strings[1], "删除"));
        if (text == null) text = new StringBuilder();

        if (strings.length >= 3 && add && Strings.isNotBlank(command)) {
            List<String> added = new ArrayList<>();
            List<String> exited = new ArrayList<>();
            for (int i = 2; i < strings.length; i++) {
                String args = strings[i].trim();
                Long count = whatService.lambdaQuery()
                        .eq(BotDoWhat::getType, command)
                        .eq(BotDoWhat::getStats, 0)
                        .eq(BotDoWhat::getName, args)
                        .count();
                if (count == 0) {
                    whatService.save(BotDoWhat.builder()
                            .type(command)
                            .name(args)
                            .userId(user.getId())
                            .stats(0)
                            .build());
                    added.add(args);
                } else {
                    exited.add(args);
                }
            }
            text = new StringBuilder();
            if (!added.isEmpty()) {
                text.append("```新增\n").append(String.join(", ", added)).append("```").append("\n");
            }
            if (!exited.isEmpty()) {
                text.append("```已存在\n").append(String.join(", ", exited)).append("```").append("\n");
            }
        }

        if (strings.length >= 3 && del && Strings.isNotBlank(command)) {
            if (Objects.equals(user.getId(), 240564004L)) {
                List<String> deleted = new ArrayList<>();
                for (int i = 2; i < strings.length; i++) {
                    String args = strings[i].trim();
                    LambdaQueryWrapper<BotDoWhat> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(BotDoWhat::getName, args);
                    wrapper.eq(BotDoWhat::getType, command);
                    wrapper.eq(BotDoWhat::getStats, 0);
                    boolean remove = whatService.remove(wrapper);
                    if (remove) {
                        deleted.add(args);
                    }
                }
                text = new StringBuilder();
                if (!deleted.isEmpty()) {
                    text.append("```已删除\n").append(String.join(", ", deleted)).append("```").append("\n");
                }
            }
        }

        // 啥都没回来的时候
        if (Strings.isBlank(text.toString())) {
            text = new StringBuilder(getDoingList(command, "1"));
        }

        sendMessage.setChatId(chatId);
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.enableMarkdown(true);
        sendMessage.setParseMode("MarkdownV2");
        sendMessage.setText(text.toString());
        if (buttonBool) {
            InlineKeyboardMarkup markup = addButton(command);
            sendMessage.setReplyMarkup(markup);
        }
        Message sender = this.sender.sender(sendMessage);
        if (!buttonBool) {
            baseService.deleteMessage(chatId, sender.getMessageId(), 10);
        }
        return Boolean.TRUE;
    }

    private InlineKeyboardMarkup addButton(String command) {
        LinkedHashMap<String, String> firstRow = new LinkedHashMap<>();
        firstRow.put(command + "-replace-1", "来一个");
        firstRow.put(command + "-replace-10", "来十个");
        LinkedHashMap<String, String> secondRow = new LinkedHashMap<>();
        secondRow.put(command + "-cgmj", "吃干抹净");
        secondRow.put(command + "-delete", "掀桌子");
        List<LinkedHashMap<String, String>> keyboard = new ArrayList<>();
        keyboard.add(firstRow);
        keyboard.add(secondRow);
        return baseService.initInlineKeyboard(keyboard);
    }

    @NotNull
    private static String getCommand(String str) {
        String pattern = "/(\\w+)";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(str);
        return matcher.find() ? matcher.group(1) : "";
    }

    @NotNull
    private String getDoingList(String type, String number) {
        String text;
        if (Integer.parseInt(number) > 20) number = "20";
        List<BotDoWhat> list = whatService.lambdaQuery()
                .eq(BotDoWhat::getType, type)
                .eq(BotDoWhat::getStats, 0)
                .last("order by RAND() limit " + number)
                .list();
        text = list.stream().map(BotDoWhat::getName).collect(Collectors.joining(", "));
        return text;
    }

    @Override
    public Boolean callbackQuery(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Message message = callbackQuery.getMessage();
        String data = callbackQuery.getData();
        int messageId = message.getMessageId();
        Chat chat = message.getChat();
        Long chatId = message.getChatId();
        User user = callbackQuery.getFrom();
        String[] callbackCommand = data.split("-");
        String command = callbackCommand[0];
        EditMessageText editMessage = new EditMessageText();
        boolean edit = false;
        editMessage.setChatId(chatId);
        editMessage.setMessageId(messageId);
        editMessage.enableMarkdown(true);
        editMessage.setParseMode("MarkdownV2");
        if (data.endsWith("-replace-1")) {
            String text = message.getText().replace("~", "");
            editMessage.setText("~" + text + ", ~" + getDoingList(command, "1"));
            edit = true;
        }
        if (data.endsWith("-replace-10")) {
            String text = message.getText().replace("~", "");
            editMessage.setText("~" + text + ", ~" + getDoingList(command, "10"));
            edit = true;
        }

        // 吃干抹净
        if (data.endsWith("-cgmj")) {
            editMessage.setText(getDoingList(command, "1"));
            edit = true;
        }

        if (data.endsWith("-delete")) {
            baseService.deleteMessage(chatId, messageId);
        }

        if (edit) {
            InlineKeyboardMarkup markup = addButton(command);
            editMessage.setReplyMarkup(markup);
            sender.sender(editMessage);
        }
        return true;
    }
}
