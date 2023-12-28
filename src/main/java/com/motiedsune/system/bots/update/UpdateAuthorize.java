package com.motiedsune.system.bots.update;

import com.motiedsune.system.bots.model.entity.BotChat;
import com.motiedsune.system.bots.model.entity.BotUser;
import com.motiedsune.system.bots.service.BotChatService;
import com.motiedsune.system.bots.service.BotUserService;
import com.motiedsune.system.bots.service.IBotBaseService;
import com.motiedsune.system.bots.service.IBotSender;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 用途：
 *
 * @author MoTIEdsuNe
 * @date 2023-12-08 星期五
 */
@Slf4j
@Component
public class UpdateAuthorize {

    @Resource
    BotUserService userService;

    @Resource
    BotChatService chatService;

    @Resource
    IBotBaseService baseService;

    @Resource
    IBotSender sender;

    /**
     * 检查是否授权
     * 1. 个人用户：强制要求授权
     * 2. 群用户：要求管理员授权
     */
    public Boolean isPersonAuthorized(Update update) {
        User user = null;
        if (update.hasMessage()) {
            Message message = update.getMessage();
            user = message.getFrom();
        }
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            user = callbackQuery.getFrom();
        }
        if (user == null) return false;
        BotUser botUser = userService.getById(user.getId());
        if (botUser == null || botUser.getAuthorize() == null) return false;
        return botUser.getAuthorize();
    }

    public Boolean isGroupAuthorized(Update update) {
        Message message = update.getMessage();
        Chat chat = message.getChat();
        if (chat.isSuperGroupChat() || chat.isGroupChat()) {
            BotChat user = chatService.getById(update.getMessage().getChatId());
            if (user == null || user.getAuthorize() == null) return false;
            return user.getAuthorize();
        }
        return true;
    }


    /**
     * 确认授权
     * - 群
     * - 已授权：return
     * - 未授权：请求授权
     * - 请求授权：若数据是 callback ：检查是否同意；若同意：执行同意添加指令；否则，弹出
     * -
     */
    public boolean confirmAuthorization(Update update) {
        Boolean flag = Boolean.FALSE;
        if (update.hasCallbackQuery()) {
            Boolean personAuthorized = isPersonAuthorized(update);
            if (!personAuthorized) {
                userService.save(new BotUser(update.getCallbackQuery().getFrom()));
                return Boolean.TRUE;
            }
            if (true) return Boolean.TRUE;
            boolean b = callbackAuthorizationCheck(update);
            if (!b) return false;
        }

        if (update.hasMessage()) {
            Message message = update.getMessage();
            User user = message.getFrom();
            Chat chat = message.getChat();
            // 如果是群且没授权
            Boolean groupAuthorized = isGroupAuthorized(update);
            if (!groupAuthorized) {
                chatService.save(new BotChat(chat, user.getId()));
                return Boolean.TRUE;

//                requestAuthorization(update, "group");
                // 请求授权并跳出
//                return Boolean.FALSE;
            }

            // 如果是个人且没授权
            Boolean personAuthorized = isPersonAuthorized(update);
            if (!personAuthorized) {
                userService.save(new BotUser(user));
                return Boolean.TRUE;
//                requestAuthorization(update, "person");
                // 请求授权并跳出
//                return Boolean.FALSE;
            }
            // 如果完成了鉴权
            flag = Boolean.TRUE;
        }

        return flag;
    }

    public boolean callbackAuthorizationCheck(Update update) {
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            Message message = callbackQuery.getMessage();
            Boolean groupCheck = callbackGroupCheck(callbackQuery, message);
            if (!groupCheck) return false; // false = 后续不再执行命令
            Boolean personCheck = callbackPersonCheck(callbackQuery, message);
            if (!personCheck) return false;
            log.info("callback not authorized");
        }
        // 其他继续
        return true;
    }

    @NotNull
    private Boolean callbackPersonCheck(CallbackQuery callbackQuery, Message message) {
        String data = callbackQuery.getData();
        User user = callbackQuery.getFrom();
        int messageId = message.getMessageId();
        Long chatId = message.getChatId();
        // 个人同意
        if (data.equals("authorized-person-success")) {
            baseService.sendCallbackQuery(callbackQuery.getId(), "您已同意存储您的信息，感谢支持！");
            baseService.deleteMessage(chatId, messageId);
            log.info(user.getId() + "同意个人存储！");
            // 此处 chat 存储数据
            userService.save(new BotUser(user));
            // 可以继续执行
            return true;
        }
        // 个人拒绝
        if (data.equals("authorized-person-fail")) {
            log.info(user.getId() + "拒绝个人存储！");
            baseService.sendCallbackQuery(callbackQuery.getId(), "您已拒绝存储个人信息，再见！");
            baseService.deleteMessage(chatId, messageId);
            // 不可继续执行
            return false;
        }
        return true;
    }

    @NotNull
    private Boolean callbackGroupCheck(CallbackQuery callbackQuery, Message message) {
        String data = callbackQuery.getData();
        int messageId = message.getMessageId();
        Chat chat = message.getChat();
        Long chatId = message.getChatId();
        User user = callbackQuery.getFrom();
        // 群同意
        if (data.equals("authorized-group-success")) {
            // 是管理员
            if (baseService.isUserAdmin(chatId, user.getId())) {
                baseService.sendCallbackQuery(callbackQuery.getId(), "您已同意存储本群信息！感谢您的支持！");
                baseService.deleteMessage(chatId, messageId);
                log.info("{} 的 {} 同意群存储！", chatId, user.getId());
                // 此处 chat 存储数据
                chatService.save(new BotChat(chat, user.getId()));
                // 可以继续执行
                return true;
            } else {
                baseService.sendCallbackQuery(callbackQuery.getId(), "禁止非管理员执行命令！");
                return false;
            }
        }
        // 群拒绝
        if (data.equals("authorized-group-fail")) {
            log.info("{} 的 {} 不同意群存储！", chatId, user.getId());
            baseService.sendCallbackQuery(callbackQuery.getId(), "您已拒绝存储本群信息，再见！");
            baseService.deleteMessage(chatId, messageId);
            // 此处退群
            baseService.leaveGroup(chatId);
            // 不可继续执行
            return false;
        }
        return true;
    }


    // 请求授权
    public void requestAuthorization(Update update, String type) {
        Message message = update.getMessage();
        LinkedHashMap<String, String> firstRow = new LinkedHashMap<>();
        SendMessage result = new SendMessage();
        if ("group".equals(type)) {
            log.info("群 {} 请求授权！", message.getChat().getId());
            result.setText("用户须知：\n本 bot 将按照运行准则，适当收集、存储、使用请求本 bot 所产生的数据，若贵群管理员同意，本 bot 将记录您的 id、名称等内容；若您同意，请点击 “同意” 按钮，否则，请点击“拒绝”按钮。");
            firstRow.put("authorized-group-success", "同意");
            firstRow.put("authorized-group-fail", "拒绝");
        }

        if ("person".equals(type)) {
            log.info("个人 {} 请求授权！", message.getFrom().getId());
            result.setText("用户须知：\n本 bot 将按照运行准则，适当收集、存储、使用请求本 bot 所产生的数据，若您同意，本 bot 将记录您的 id、名称等内容；若您同意，请点击 “同意” 按钮，否则，请点击“拒绝”按钮。");
            firstRow.put("authorized-person-success", "同意");
            firstRow.put("authorized-person-fail", "拒绝");
        }
        // 在这里实现请求用户授权的逻辑
        result.setChatId(message.getChatId());
        result.setReplyToMessageId(message.getMessageId());
        List<LinkedHashMap<String, String>> keyboard = new ArrayList<>();
        keyboard.add(firstRow);
        InlineKeyboardMarkup keyboardMarkup = baseService.initInlineKeyboard(keyboard);
        result.setReplyMarkup(keyboardMarkup);
        sender.sender(result);
    }
}
