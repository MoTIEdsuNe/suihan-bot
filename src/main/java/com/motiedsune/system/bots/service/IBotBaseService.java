package com.motiedsune.system.bots.service;

import com.motiedsune.system.bots.model.entity.BotUser;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 用途：
 *
 * @author MoTIEdsuNe
 * @date 2023-12-12 星期二
 */
public interface IBotBaseService {


    // 判断用户是否是群管理员
    boolean isUserAdmin(long chatId, long userId);

    // 退出某 chat
    void leaveGroup(long chatId);

    // 发送按钮返回浮窗
    void sendCallbackQuery(String callbackQueryId, String text);

    void deleteMessage(Long chatId, int messageId, int second);

    // 删除消息
    void deleteMessage(Long chatId, int messageId);

    /**
     * 初始化按钮消息</br>
     * 格式为：</br>
     * new List </br>
     * 行1：new Map
     * 行1列1：Map.Entity<key,value>
     * 行2：new Map ...
     */
    InlineKeyboardMarkup initInlineKeyboard(List<LinkedHashMap<String, String>> innerData);

    Message sendMessage(Long chatId, int messageId, String msg);

    // 发送特定秒后删除的消息
    void sendMessage(Long chatId, int messageId, String msg, int existSecond);

    String getUserInfo(User replyUser);

    String getUserInfo(BotUser user);


    SendMessage createMarkdownMessage(Long chatId, Integer messageId, String msg);

    EditMessageText createEditMarkdownMessage(Long chatId, Integer messageId, String msg);

    List<String> passSymbols();
}
