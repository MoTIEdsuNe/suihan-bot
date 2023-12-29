package com.motiedsune.system.bots.update.command;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.motiedsune.system.bots.model.entity.BotDoWhat;
import com.motiedsune.system.bots.service.BotDoWhatService;
import com.motiedsune.system.bots.service.IBotBaseService;
import com.motiedsune.system.bots.service.IBotSender;
import com.motiedsune.system.bots.update.IUpdateCommand;
import com.motiedsune.system.bots.utils.StrUtils;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
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
@Slf4j
@Service
public class UpdateCommandDoingWhat implements IUpdateCommand {

    private final String pattern = "[^\\p{InCJKUnifiedIdeographs}\\p{IsHan}\\p{IsLatin}\\p{IsHiragana}\\p{IsKatakana}]+";
    private final Pattern regex = Pattern.compile(pattern);

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

        InlineKeyboardMarkup markup = null;

        String command = getCommand(strings[0]);

        // 就一个参数
        if (strings.length == 1 && Strings.isNotBlank(command)) {
            text = new StringBuilder(getDoingList(command, "1"));
            markup = buttonPlay(command);
            buttonBool = true;
        }
        // 如果执行了，那就标记为 true
        boolean firstCommandBool = false;
        String firstCommand = strings.length > 1 ? strings[1].toLowerCase() : "";
        boolean add = strings.length > 1
                && (StringUtils.equals(firstCommand, "add")
                || StringUtils.equals(firstCommand, "insert")
                || StringUtils.equals(firstCommand, "添加")
                || StringUtils.equals(firstCommand, "插入"));

        boolean del = strings.length > 1
                && (StringUtils.equals(firstCommand, "del")
                || StringUtils.equals(firstCommand, "删除"));

        boolean page = strings.length > 1
                && (StringUtils.equals(firstCommand, "页数")
                || StringUtils.equals(firstCommand, "page")
                || StringUtils.equals(firstCommand, "list")
                || StringUtils.equals(firstCommand, "列表"));

        if (add || del || page) firstCommandBool = true;

        if (text == null) text = new StringBuilder();

        if (strings.length >= 3 && add && Strings.isNotBlank(command)) {
            List<String> added = new ArrayList<>();
            List<String> exited = new ArrayList<>();
            List<String> notSatisfied = new ArrayList<>();
            List<String> datalist = new ArrayList<>();
            filterText(strings, datalist, notSatisfied);
            if (!datalist.isEmpty()) {
                List<BotDoWhat> dblist = whatService.lambdaQuery()
                        .eq(BotDoWhat::getType, command)
                        .eq(BotDoWhat::getStats, 0)
                        .in(BotDoWhat::getName, datalist)
                        .list();
                exited = dblist.stream().map(BotDoWhat::getName).toList();
                datalist.removeAll(exited);
                added = datalist;
            }
            if (!added.isEmpty()) {
                List<BotDoWhat> collect = added.stream().map(d -> BotDoWhat.builder()
                        .type(command).name(d).userId(user.getId()).stats(0).build()).toList();
                whatService.saveBatch(collect);
            }


            text = new StringBuilder();
            if (!added.isEmpty()) {
                text.append("```新增\n").append(String.join(", ", added)).append("```").append("\n");
            }
            if (!exited.isEmpty()) {
                text.append("```已存在\n").append(String.join(", ", exited)).append("```").append("\n");
            }
            if (!exited.isEmpty()) {
                text.append("```不满足条件\n").append(notSatisfied.stream().map(d ->
                        baseService.formatMarkdownV2(d)).collect(Collectors.joining(", "))).append("```").append("\n");
            }
        }

        if (strings.length >= 3 && del && Strings.isNotBlank(command)) {
            List<String> notSatisfied = new ArrayList<>();
            List<String> datalist = new ArrayList<>();
            filterText(strings, datalist, notSatisfied);
            List<String> deleted = this.whatService.lambdaQuery()
                    .eq(BotDoWhat::getType, command)
                    .eq(BotDoWhat::getStats, 0)
                    .in(BotDoWhat::getName, datalist)
                    .list()
                    .stream()
                    // 要么是用户本身，要么是我
                    .filter(data -> Objects.equals(data.getUserId(), user.getId()) || Objects.equals(user.getId(), 240564004L))
                    .map(BotDoWhat::getName)
                    .toList();
            if (!deleted.isEmpty()) {
                LambdaQueryWrapper<BotDoWhat> wrapper = new LambdaQueryWrapper<>();
                wrapper.in(BotDoWhat::getName, deleted);
                wrapper.eq(BotDoWhat::getType, command);
                wrapper.eq(BotDoWhat::getStats, 0);
                boolean remove = whatService.remove(wrapper);
                text = new StringBuilder();
                if (remove) {
                    text.append("```已删除\n").append(String.join(", ", deleted)).append("```").append("\n");
                } else {
                    text.append("删除失败！");
                }
            }

        }

        if (strings.length >= 2 && page && Strings.isNotBlank(command)) {
            long current = 1;
            long size = 20;

            if (strings.length >= 3) {
                // 如果是正整数
                if (StrUtils.isPositiveInteger(strings[2])) {
                    current = Long.parseLong(strings[2]);
                }
            }

            if (strings.length >= 4) {
                if (StrUtils.isPositiveInteger(strings[3])) {
                    size = Long.parseLong(strings[3]);
                    if (size > 100) size = 100;
                }
            }

            Page<BotDoWhat> data = this.whatService.lambdaQuery()
                    .eq(BotDoWhat::getType, command)
                    .eq(BotDoWhat::getStats, 0)
                    .page(new Page<>(current, size));

            List<BotDoWhat> datalist = data.getRecords();

            if (ObjectUtils.isEmpty(datalist)) {
                text.append("这里是寂静的荒原, 池子里只有 ").append(data.getTotal()).append(" 个身影在等着你，你离开太久了。（没这么多数据！）");
            } else {
                text.append("```列表：\n")
                        .append(datalist.stream()
                                .map(BotDoWhat::getName)
                                .collect(Collectors.joining("\n")))
                        .append("```")
                        .append("合计：").append(data.getTotal()).append("条哦！")
                        .append("\n");
            }

            LinkedHashMap<String, String> firstRow = new LinkedHashMap<>();
            if (current != 1 && !ObjectUtils.isEmpty(datalist))
                firstRow.put(command + "-page-" + (current - 1) + "-" + size, "上一页");
            // 不为空 且 当前页面足够（至少下一页有东西）
            if (!ObjectUtils.isEmpty(datalist) && (double) data.getTotal() / size > current)
                firstRow.put(command + "-page-" + (current + 1) + size, "下一页");
            LinkedHashMap<String, String> secondRow = new LinkedHashMap<>();
            secondRow.put(command + "-page-1-" + size, "第一页");
            secondRow.put(command + "-delete", "掀桌子");
            List<LinkedHashMap<String, String>> keyboard = new ArrayList<>();
            keyboard.add(firstRow);
            keyboard.add(secondRow);
            markup = baseService.initInlineKeyboard(keyboard);
            buttonBool = true;

        }
        // 啥都没回来的时候

        if (strings.length >= 2 && !firstCommandBool && Strings.isNotBlank(command)) {
            if (filterText(strings[1])) {
                text = new StringBuilder(getDoingList(command, "1", strings[1]));
            }
        }

        if (Strings.isBlank(text.toString())) {
            text = new StringBuilder(getDoingList(command, "1"));
        }

        sendMessage.setChatId(chatId);
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.enableMarkdown(true);
        sendMessage.setParseMode("MarkdownV2");
        sendMessage.setText(text.toString());
        if (buttonBool) {
            sendMessage.setReplyMarkup(markup);
        }
        Message sender = this.sender.sender(sendMessage);
        if (!buttonBool) {
            baseService.deleteMessage(chatId, sender.getMessageId(), 10);
        }
        return Boolean.TRUE;
    }


    private boolean filterText(String data) {
        Matcher matcher = regex.matcher(data);
        return !matcher.find();
    }

    private void filterText(String[] strings, List<String> datalist, List<String> notSatisfied) {
        for (int i = 2; i < strings.length; i++) {
            String data = strings[i];
            Matcher matcher = regex.matcher(data);
            // 判断是否匹配到非中文符号
            if (matcher.find()) {
                log.warn("【过滤】字符串中包含非中、英、日文符号: {}", data);
                notSatisfied.add(data);
            } else {
                datalist.add(data);
            }
        }
    }

    private InlineKeyboardMarkup buttonPlay(String command) {
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

//    private InlineKeyboardMarkup buttonPage(String command) {
//
//    }

    @NotNull
    private static String getCommand(String str) {
        String pattern = "/(\\w+)";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(str);
        return matcher.find() ? matcher.group(1) : "";
    }

    @NotNull
    private String getDoingList(String type, String number) {
        return getDoingList(type, number, null);
    }

    private String getDoingList(String type, String number, String keyword) {
        String text;
        if (Integer.parseInt(number) > 20) number = "20";
        List<BotDoWhat> list = whatService.lambdaQuery()
                .eq(BotDoWhat::getType, type)
                .eq(BotDoWhat::getStats, 0)
                .like(Strings.isNotBlank(keyword), BotDoWhat::getName, keyword)
                .last("order by RAND() limit " + number)
                .list();
        text = list.stream().map(BotDoWhat::getName).collect(Collectors.joining(", "));
        if (Strings.isBlank(text)) text = "这里是一片未知的荒原，还什么都没有添加呢！";
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

        if (callbackCommand.length >= 2 && callbackCommand[1].equals("page")) {
            long current = Long.parseLong(callbackCommand[2]);
            long size = Long.parseLong(callbackCommand[3]);
            Page<BotDoWhat> page = this.whatService.lambdaQuery()
                    .eq(BotDoWhat::getType, command)
                    .eq(BotDoWhat::getStats, 0)
                    .page(new Page<>(current, size));

            List<BotDoWhat> datalist = page.getRecords();
            StringBuilder text = new StringBuilder();
            if (ObjectUtils.isEmpty(datalist)) {
                text.append("好像出了点问题的样子，要不你问问狸狸？");
            } else {
                text.append("```列表：\n")
                        .append(datalist.stream()
                                .map(BotDoWhat::getName)
                                .collect(Collectors.joining("\n")))
                        .append("```")
                        .append("\n");
            }
            editMessage.setText(text.toString());
            edit = true;
        }

        if (edit) {
            InlineKeyboardMarkup markup = buttonPlay(command);
            editMessage.setReplyMarkup(markup);
            sender.sender(editMessage);
        }
        return true;
    }
}
