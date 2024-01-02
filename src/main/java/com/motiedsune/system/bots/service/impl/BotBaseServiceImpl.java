package com.motiedsune.system.bots.service.impl;

import com.motiedsune.system.bots.model.entity.BotUser;
import com.motiedsune.system.bots.scheduler.BotDeleteMessageJob;
import com.motiedsune.system.bots.service.IBotBaseService;
import com.motiedsune.system.bots.service.IBotSender;
import com.motiedsune.system.bots.utils.MarkdownUtils;
import com.motiedsune.system.bots.utils.QuartzSchedulerUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.Asserts;
import org.apache.logging.log4j.util.Strings;
import org.quartz.DateBuilder;
import org.quartz.JobBuilder;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.groupadministration.LeaveChat;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberAdministrator;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberOwner;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * 用途：
 *
 * @author MoTIEdsuNe
 * @date 2023-12-12 星期二
 */
@Slf4j
@Service
public class BotBaseServiceImpl implements IBotBaseService {

    @Resource
    IBotSender sender;

    @Override
    public boolean isUserAdmin(long chatId, long userId) {
        GetChatAdministrators getChatAdministrators = new GetChatAdministrators();
        getChatAdministrators.setChatId(chatId);

        List<ChatMember> chatMembers = sender.sender(getChatAdministrators, "判断群组管理员失败！");
        if (chatMembers == null) return false;
        for (ChatMember chatMember : chatMembers) {
            User user = chatMember.getUser();
            if (user != null && user.getId() == userId) {
                if (chatMember instanceof ChatMemberAdministrator || chatMember instanceof ChatMemberOwner) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void leaveGroup(long chatId) {
        LeaveChat leaveChat = new LeaveChat();
        leaveChat.setChatId(chatId);
        sender.sender(leaveChat, "退出群失败！");
    }

    @Override
    public void sendCallbackQuery(String callbackQueryId, String text) {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(callbackQueryId);
        answerCallbackQuery.setText(text);
        sender.sender(answerCallbackQuery);
    }

    @Override
    public void deleteMessage(Long chatId, int messageId, int second) {
        QuartzSchedulerUtils.create(
                        () ->
                                JobBuilder.newJob(BotDeleteMessageJob.class)
                                        .usingJobData("chatId", chatId)
                                        .usingJobData("messageId", messageId)
                                        .withIdentity(chatId + "_" + messageId
                                                , "BOTS_DELETE_MESSAGE_DETAIL_GROUP")
                                        .build(),
                        () ->
                                TriggerBuilder.newTrigger()
                                        .withIdentity(chatId + "_" + messageId
                                                , "BOTS_DELETE_MESSAGE_TRIGGER_GROUP")
                                        .startAt(DateBuilder.futureDate(second, DateBuilder.IntervalUnit.SECOND))
                                        .build())
                .schedule();
    }

    @Override
    public void deleteMessage(Long chatId, int messageId) {
        DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(chatId), messageId);
        sender.sender(deleteMessage);
    }

    @Override
    public InlineKeyboardMarkup initInlineKeyboard(List<LinkedHashMap<String, String>> innerData) {
        List<List<InlineKeyboardButton>> collect = innerData.stream()
                .map(map -> {
                    List<InlineKeyboardButton> buttons = new LinkedList<>();
                    map.forEach((key, value) -> {
                        InlineKeyboardButton button = new InlineKeyboardButton(value);
                        button.setCallbackData(key);
                        buttons.add(button);
                    });
                    return buttons;
                }).toList();

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(collect);
        return keyboard;
    }

    @Override
    public Message sendMessage(Long chatId, int messageId, String msg) {
        SendMessage sendMessage = createMarkdownMessage(chatId, messageId, msg);
        return sender.sender(sendMessage);
    }

    @Override
    public void sendMessage(Long chatId, int messageId, String msg, int existSecond) {
        Message message = this.sendMessage(chatId, messageId, msg);
        this.deleteMessage(chatId, message.getMessageId(), existSecond);
    }

    @Override
    public String getUserInfo(User replyUser) {
        String firstName = Strings.isNotBlank(replyUser.getFirstName()) ? replyUser.getFirstName() : "";
        String lastName = Strings.isNotBlank(replyUser.getLastName()) ? replyUser.getLastName() : "";
        Long id = replyUser.getId();
//        return "[" + firstName + " " + lastName + "](tg://user?id=" + id + ")";
        return MarkdownUtils.create().inlineUser(firstName + " " + lastName, id).build().toString();
    }

    @Override
    public String getUserInfo(BotUser user) {
        String firstName = Strings.isNotBlank(user.getFirstName()) ? user.getFirstName() : "";
        Long id = user.getId();
//        return "[" + firstName + "](tg://user?id=" + id + ")";
        return MarkdownUtils.create().inlineUser(firstName, id).build().toString();
    }


    @Override
    public String formatMarkdownV2(String msg) {
        String[] symbols = {
                "_",
                "*",
//                "[", "]",
//                "(", ")",
//                "{", "}",
                "~",
//                "`",
                ">",
                "#",
                "+",
                "-",
                "=",
                "|",
                ".",
                "!"
        };
        for (String symbol : symbols) {
            if (msg.contains(symbol)) {
                msg = msg.replace(symbol, "\\" + symbol);
            }
        }
        return msg;
    }

    @Override
    public SendMessage createMarkdownMessage(Long chatId, Integer messageId, String msg) {
        Asserts.notNull(chatId, "chatId 不可为空！");
        Asserts.notNull(msg, "发送的消息不可为空！");
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        if (messageId != null) {
            sendMessage.setReplyToMessageId(messageId);
        }
        sendMessage.setParseMode("MarkdownV2");
        String text = this.formatMarkdownV2(msg);
        sendMessage.setText(text);
        return sendMessage;
    }

    @Override
    public EditMessageText createEditMarkdownMessage(Long chatId, Integer messageId, String msg) {
        Asserts.notNull(chatId, "chatId 不可为空！");
        Asserts.notNull(msg, "发送的消息不可为空！");
        EditMessageText sendMessage = new EditMessageText();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setMessageId(messageId);
        sendMessage.setParseMode("MarkdownV2");
        String text = this.formatMarkdownV2(msg);
        sendMessage.setText(text);
        return sendMessage;
    }
}
