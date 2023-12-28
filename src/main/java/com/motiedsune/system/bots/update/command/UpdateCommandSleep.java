package com.motiedsune.system.bots.update.command;

import com.motiedsune.system.bots.update.IUpdateCommand;
import com.motiedsune.system.bots.utils.BotUtils;
import com.motiedsune.system.bots.model.entity.BotChat;
import com.motiedsune.system.bots.model.entity.BotSleep;
import com.motiedsune.system.bots.model.entity.BotUser;
import com.motiedsune.system.bots.model.enums.Status;
import com.motiedsune.system.bots.scheduler.BotSleepJob;
import com.motiedsune.system.bots.service.*;
import com.motiedsune.system.bots.utils.QuartzSchedulerUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.Asserts;
import org.apache.logging.log4j.util.Strings;
import org.quartz.*;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 用途：
 *
 * @author MoTIEdsuNe
 * @date 2023-12-21 星期四
 */
@Service
@Slf4j
public class UpdateCommandSleep implements IUpdateCommand {

    @Resource
    IBotSender sender;

    @Resource
    IBotBaseService baseService;

    @Resource
    BotChatService chatService;

    @Resource
    BotSleepService sleepService;

    @Resource
    BotUserService userService;

    @Override
    public Set<String> commands() {
        return Collections.singleton("sleep");
    }

    @Override
    public Boolean consume(Update update) {
        if (!update.hasMessage()) return false;
        Message message = update.getMessage();
        User user = message.getFrom();
        Chat chat = message.getChat();
        Long chatId = message.getChatId();
        Integer messageId = message.getMessageId();

        if (!message.isReply()) {
            baseService.sendMessage(chatId, messageId, "要催谁睡觉觉呢？\n\n" +
                    "用法：`/sleep` + 回复的内容（可留空），\n" +
                    "例如：`{self}` 向 `{friend}` 道晚安\n\n" +
                    "参数: `{self}`:自己， `{friend}`:对方\n" +
                    "", 30);
            return false;
        }
        Message replyToMessage = message.getReplyToMessage();
        User replyUser = replyToMessage.getFrom();
        if (replyUser.getIsBot()) {
            baseService.sendMessage(chatId, messageId, "不可以催 bot 睡觉啦！", 30);
            return false;
        }
        String userInfo = baseService.getUserInfo(replyUser);
        Boolean edited = Boolean.FALSE;
        // 首先查询A对B是否已有配置 sleep
        BotSleep sleep = sleepService.findSleep(user.getId(), replyUser.getId());
        StringBuilder text = new StringBuilder();
        Map<String, String> commands = BotUtils.parseCommands(message.getText());
        String args = commands.getOrDefault("sleep", null);
        if (sleep == null) {
            // 如果不存在 sleep 则新建。
            text = new StringBuilder("喵星督促睡觉小助手初始化中 ...\n\n");
            sleep = BotSleep.builder()
                    .chatId(chatId)
                    .fromUserId(user.getId())
                    .toUserId(replyUser.getId())
                    .startTime(LocalDateTime.now())
                    // 默认只执行七天
                    .endTime(LocalDateTime.now().plusDays(1))
                    .timeZone("UTC+8")
                    .length(1)
                    .frequency(1)
                    .cron("1 2 23 * * ?")
                    .status(Status.DISABLE)
                    .build();
            edited = true;
        }

        if (!Objects.equals(sleep.getChatId(), chatId)) {
            sleep.setChatId(chatId);
            edited = true;
        }

        if (Strings.isNotBlank(args)) {
            sleep.setMsg(args);
            edited = true;
        }

        if (edited) {
            this.sleepService.saveOrUpdate(sleep);
        }
        buildStatusInfo(sleep, text);
        InlineKeyboardMarkup markup = buttonBase(sleep);
        SendMessage sendMessage = baseService.createMarkdownMessage(chatId, messageId, text.toString());
        sendMessage.setReplyMarkup(markup);
        sendMessage.disableNotification();
        sendMessage.enableNotification();
        sender.sender(sendMessage);
        return true;
    }

    private void buildStatusInfo(BotSleep sleep, StringBuilder text) {
        if (text == null) return;
        // 返回 sleep 状态
        String cronSplit2 = Strings.isNotBlank(sleep.getCron()) ? sleep.getCron().split(" ")[2] : "未设置 ";
        BotChat botChat = sleep.getChatId() != null ? this.chatService.getById(sleep.getChatId()) : null;
        String chatName = botChat != null ? botChat.getTitle() : "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (Status.ENABLE.equals(sleep.getStatus()) && LocalDateTime.now().isAfter(sleep.getEndTime())) {
            sleep.setStatus(Status.DISABLE);
            sleepService.saveOrUpdate(sleep);
        }
        text
                .append("\t当前状态：").append(sleep.getStatus().getLabel()).append("\n")
                .append("\t起始时间：").append(sleep.getStartTime().format(formatter)).append("\n")
                .append("\t结束时间：").append(sleep.getEndTime().format(formatter)).append("\n")
                .append("\t提醒时间：").append(cronSplit2).append(" 点\n")
                .append("\t提醒时区：").append(sleep.getTimeZone()).append("\n")
                .append("\t所在群组：").append(chatName);
    }


    @Override
    public Boolean callbackQuery(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Message message = callbackQuery.getMessage();
        String data = callbackQuery.getData();
        Message replyToMessage = message.getReplyToMessage();
        Long chatId = message.getChatId();
        Integer messageId = message.getMessageId();
        User nowUser = callbackQuery.getFrom();
        User replyUser = replyToMessage.getFrom();
        String[] callbackCommand = data.split("-");
        BotSleep sleep = sleepService.findSleep(nowUser.getId(), replyUser.getId());
        // result
        Boolean edited = Boolean.FALSE;
        InlineKeyboardMarkup markup = null;
        StringBuilder text = new StringBuilder();

        if (sleep == null) {
            // 如果不存在 sleep 则新建。
            text = new StringBuilder("喵星督促睡觉小助手初始化中 ...\n\n");
            sleep = BotSleep.builder()
                    .chatId(chatId)
                    .fromUserId(nowUser.getId())
                    .toUserId(replyUser.getId())
                    .startTime(LocalDateTime.now())
                    // 默认只执行七天
                    .endTime(LocalDateTime.now().plusDays(1))
                    .timeZone("UTC+8")
                    .length(1)
                    .frequency(1)
                    .cron("1 2 23 * * ?")
                    .status(Status.DISABLE)
                    .build();
            this.sleepService.save(sleep);
        }
        // 启动
        if (callbackCommand.length >= 2 && callbackCommand[1].equals("start")) {
            BotSleep finalSleep = sleep;
            sleep.setStatus(Status.ENABLE);
            sleepService.saveOrUpdate(sleep);
            BotUser fromUser = userService.getById(sleep.getFromUserId());
            String fromName = fromUser != null ? baseService.getUserInfo(fromUser) : "";
            String toName = baseService.getUserInfo(replyUser);
            String sleepMsg = Strings.isNotBlank(sleep.getMsg()) ? sleep.getMsg() : "{friend} 您的好朋友 {self} 真挚的提醒您 ~ ; 晚安时间到了，该休息了，做个好梦哦！";
            sleepMsg = sleepMsg.replace("{friend}", "{1}").replace("{self}", "{0}");
            String msg = "喵星晚安协会提醒您：\n" + MessageFormat.format(sleepMsg, fromName, toName);

            String timeZoneStr = Strings.isNotBlank(sleep.getTimeZone()) ? sleep.getTimeZone().replace("UTC", "GMT") : "GMT+8:00";
            timeZoneStr = timeZoneStr.contains("GMT") ? timeZoneStr : "GMT+8:00";
            TimeZone timeZone = TimeZone.getTimeZone(timeZoneStr);
            QuartzSchedulerUtils.create(
                            () ->
                                    JobBuilder.newJob(BotSleepJob.class)
                                            .usingJobData("chatId", chatId)
                                            .usingJobData("messageId", messageId)
                                            .usingJobData("message", msg)
                                            .withIdentity(nowUser.getId() + "_" + replyUser.getId()
                                                    , "BOTS_SLEEP_DETAIL_GROUP")
                                            .build(),
                            () ->
                                    TriggerBuilder.newTrigger()
                                            .withIdentity(nowUser.getId() + "_" + replyUser.getId()
                                                    , "BOTS_SLEEP_TRIGGER_GROUP")
                                            .withSchedule(CronScheduleBuilder.cronSchedule(finalSleep.getCron())
//                                            .withSchedule(CronScheduleBuilder.cronSchedule("1 * 22 * * ?")
                                                            .withMisfireHandlingInstructionDoNothing()// 不触发立即执行；等待下次Cron触发频率到达时刻开始按照Cron频率依次执行
                                                            .inTimeZone(timeZone) // 设置执行时区
                                            )
                                            .startAt(Date.from(finalSleep.getStartTime().atZone(ZoneId.systemDefault()).toInstant()))
                                            .endAt(Date.from(finalSleep.getEndTime().atZone(ZoneId.systemDefault()).toInstant()))
                                            .build())
                    .schedule();
            baseService.sendCallbackQuery(callbackQuery.getId(), "启动了哦~");
            edited = Boolean.TRUE;
        }

        // 停止
        if (!edited && callbackCommand.length >= 2 && callbackCommand[1].equals("stop")) {

            sleep.setStatus(Status.DISABLE);
            sleepService.saveOrUpdate(sleep);
            QuartzSchedulerUtils.create().withJobDetail(
                            () -> JobBuilder.newJob(BotSleepJob.class)
                                    .withIdentity(nowUser.getId() + "_" + replyUser.getId(), "BOTS_SLEEP_DETAIL_GROUP")
                                    .build())
                    .interrupt();
            baseService.sendCallbackQuery(callbackQuery.getId(), "停止了哦~");
            edited = Boolean.TRUE;
        }

        // 回到首页
        if (!edited && callbackCommand.length >= 2 && callbackCommand[1].equals("base")) {
            markup = buttonBase(sleep);
            buildStatusInfo(sleep, text);
            edited = Boolean.TRUE;
        }

        // 设置
        if (!edited && callbackCommand.length >= 2 && callbackCommand[1].equals("setting")) {
            // 如果是设置本身
            if (callbackCommand.length == 2) {
                markup = buttonSetting(sleep);
                edited = Boolean.TRUE;
            }
            // 如果是设置内容
            // 设置时区
            if (!edited && callbackCommand[2].equals("timezone")) {
                if (callbackCommand.length == 3) {
                    markup = buttonTimeZone();
                } else {
                    String timezone = callbackCommand[3].replace("p", "+").replace("s", "-");
                    sleep.setTimeZone(timezone);
                    this.sleepService.saveOrUpdate(sleep);
                    markup = buttonSetting(sleep);
                }
                edited = Boolean.TRUE;
            }

            // 设置时间
            if (!edited && callbackCommand[2].equals("time")) {
                if (callbackCommand.length == 3) {
                    markup = buttonTimes();
                } else {
                    String[] cron = sleep.getCron().split(" ");
                    if (cron.length != 6) sleep.setCron("* * " + callbackCommand[3] + " * * ?");
                    else {
                        cron[2] = callbackCommand[3];
                        sleep.setCron(String.join(" ", cron));
                    }
                    ;
                    this.sleepService.saveOrUpdate(sleep);
                    markup = buttonSetting(sleep);
                }
                edited = Boolean.TRUE;
            }

            // 设置提醒多久（天、周、月）
            if (!edited && callbackCommand[2].equals("duration")) {
                if (callbackCommand.length == 3) {
                    markup = buttonDuration();
                } else {
                    // 异常长度
                    if (callbackCommand.length == 4) {
                        baseService.sendCallbackQuery(callbackQuery.getId(), "系统异常！");
                        log.warn("咋来的这个参数？");
                    } else { // 正常长度
                        String date = callbackCommand[3];
                        String length = callbackCommand[4];
                        long len = Strings.isNotBlank(length) ? Long.parseLong(length) : 1L;
                        LocalDateTime now = LocalDateTime.now();
                        LocalDateTime naw;
                        sleep.setCreateTime(now);
                        naw = switch (date) {
                            case "month" -> now.plusMonths(len);
                            case "week" -> now.plusWeeks(len);
                            case "day" -> now.plusDays(len);
                            default -> now.plusDays(1L);
                        };
                        sleep.setEndTime(naw);
                        this.sleepService.saveOrUpdate(sleep);
                    }
                }
                edited = Boolean.TRUE;
            }
            // 设置间隔长度
            if (!edited && callbackCommand[2].equals("interval")) {
                if (callbackCommand.length == 3) {
                    baseService.sendCallbackQuery(callbackQuery.getId(), "系统异常！");
                    edited = Boolean.TRUE;
                }

                if (!edited && callbackCommand[3].equals("length")) {
                    if (callbackCommand.length == 4) {
                        markup = buttonLength();
                    } else {// 业务逻辑
                        String length = callbackCommand[4];
                        Integer len = Integer.valueOf(length);
                        sleep.setLength(len);
                        formatIntervalCron(sleep);
                        markup = buttonSetting(sleep);
                    }
                    edited = Boolean.TRUE;
                }
                if (!edited && callbackCommand[3].equals("frequency")) {
                    if (callbackCommand.length == 4) {
                        markup = buttonFrequency();
                    } else {// 业务逻辑
                        String fre = callbackCommand[4];
                        Integer frequency = Integer.valueOf(fre);
                        sleep.setFrequency(frequency);
                        formatIntervalCron(sleep);
                        markup = buttonSetting(sleep);
                    }
                    edited = Boolean.TRUE;
                }
            }
            // 提醒所在群
            if (!edited && callbackCommand[2].equals("chat")) {
                if (callbackCommand.length == 3) {
                    markup = buttonChat(sleep, message);
                } else {// 业务逻辑
                    switch (callbackCommand[3]) {
                        case "origin":
                            break;
                        case "new":
                            sleep.setChatId(chatId);
                            break;
                    }
                    this.sleepService.saveOrUpdate(sleep);
                }
                edited = Boolean.TRUE;
            }
        }

        // 编辑了才修改
        if (edited) {
            if (text.isEmpty()) buildStatusInfo(sleep, text);
            if (markup == null) markup = buttonBase(sleep);
            EditMessageText editMessage = baseService.createEditMarkdownMessage(chatId, messageId, text.toString());
            editMessage.setReplyMarkup(markup);
            sender.sender(editMessage);
        }
        return Boolean.TRUE;
    }


    private void formatIntervalCron(BotSleep sleep) {
        Asserts.notNull(sleep, "SLEEP 不可为空！");
        String cron = sleep.getCron();
        String[] cons = cron.split(" ");
        Random random = new Random();
//        if(cons.length != 6)
        if (sleep.getFrequency() == null) sleep.setFrequency(1);
        if (sleep.getLength() == null) sleep.setLength(1);
        if (sleep.getFrequency() == 1 && sleep.getLength() == 1) {
            cons[0] = String.valueOf(random.nextInt(60));
            cons[1] = String.valueOf(random.nextInt(3));
        } else if (sleep.getFrequency() == 1) {
            cons[0] = String.valueOf(random.nextInt(60));
            cons[1] = String.valueOf(random.nextInt(3));
        } else {
            int init = random.nextInt(3);
            int fre = sleep.getFrequency();
            int len = sleep.getLength();
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < fre; i++) {
                if (i > 0) {
                    result.append(",");
                }
                result.append(init);
                init += len;
            }
            String output = result.toString();
            cons[0] = String.valueOf(random.nextInt(60));
            cons[1] = output;
        }

        sleep.setCron(String.join(" ", cons));
        sleepService.saveOrUpdate(sleep);
    }


    private InlineKeyboardMarkup buttonBase(BotSleep sleep) {
        LinkedHashMap<String, String> line1 = new LinkedHashMap<>();
        if (Status.DISABLE.equals(sleep.getStatus())) {
            line1.put("sleep-start", "启动");
            line1.put("sleep-setting", "设置");
        }
        if (Status.ENABLE.equals(sleep.getStatus())) {
            line1.put("sleep-stop", "停止");
        }
        ArrayList<LinkedHashMap<String, String>> innerData = new ArrayList<>(List.of(line1));
        return baseService.initInlineKeyboard(innerData);
    }

    private InlineKeyboardMarkup buttonSetting(BotSleep sleep) {
        String base = "sleep-setting";
        LinkedHashMap<String, String> line1 = new LinkedHashMap<>();
        String timezone = "（" + (Strings.isNotBlank(sleep.getTimeZone()) ? sleep.getTimeZone() : "暂无") + "）";
        String timeHour = "（" + (Strings.isNotBlank(sleep.getCron()) ? sleep.getCron().split(" ")[2] + " 时" : "未设置 ") + "）";
        line1.put(base + "-timezone", "时区" + timezone);
        line1.put(base + "-time", "时间" + timeHour);
        line1.put(base + "-duration", "时长");// 1天、1周、1个月
        LinkedHashMap<String, String> line2 = new LinkedHashMap<>();
        line2.put(base + "-interval-length", "间隔长度");
        line2.put(base + "-interval-frequency", "提醒次数");
        line2.put(base + "-chat", "提醒位置");
        LinkedHashMap<String, String> line3 = new LinkedHashMap<>();
        line3.put("sleep-base", "返回首页");
        ArrayList<LinkedHashMap<String, String>> innerData = new ArrayList<>(List.of(line1, line2, line3));
        return baseService.initInlineKeyboard(innerData);
    }

    private InlineKeyboardMarkup buttonTimeZone() {
        String base = "sleep-setting-timezone";
        LinkedHashMap<String, String> line1 = new LinkedHashMap<>();
        line1.put(base + "-GMTp8", "默认（UTC+8）");
        LinkedHashMap<String, String> line2 = new LinkedHashMap<>();
        line2.put(base + "-UTCs12", "UTC-12(IDLW)");
        line2.put(base + "-UTCs11", "UTC-11(SST)");
        line2.put(base + "-UTCs10", "UTC-10(HST)");
        line2.put(base + "-UTCs930", "UTC-9:30(MIT)");
        LinkedHashMap<String, String> line3 = new LinkedHashMap<>();
        line3.put(base + "-UTCs9", "UTC-9(AKST)");
        line3.put(base + "-UTCs8", "UTC-8(PST)");
        line3.put(base + "-UTCs7", "UTC-7(MST)");
        line3.put(base + "-UTCs6", "UTC-6(CST)");
        LinkedHashMap<String, String> line4 = new LinkedHashMap<>();
        line4.put(base + "-UTCs5", "UTC-5(EST)");
        line4.put(base + "-UTCs4", "UTC-4(AST)");
        line4.put(base + "-UTCs330", "UTC-3:30(NST)");
        line4.put(base + "-UTCs3", "UTC-3(BRT)");
        LinkedHashMap<String, String> line5 = new LinkedHashMap<>();
        line5.put(base + "-UTCs2", "UTC-2(FNT)");
        line5.put(base + "-UTCs1", "UTC-1(CVT)");
        line5.put(base + "-UTC0", "UTC-0(WET|GMT)");
        line5.put(base + "-UTCp1", "UTC+1(CET)");
        LinkedHashMap<String, String> line6 = new LinkedHashMap<>();
        line6.put(base + "-UTCp2", "UTC+2(EET)");
        line6.put(base + "-UTCp3", "UTC+3(MSK)");
        line6.put(base + "-UTCp330", "UTC+3:30(IRST)");
        line6.put(base + "-UTCp4", "UTC+4(GST)");
        LinkedHashMap<String, String> line7 = new LinkedHashMap<>();
        line7.put(base + "-UTCp5", "UTC+5(PKT)");
        line7.put(base + "-UTCp530", "UTC+5:30(IST)");
        line7.put(base + "-UTCp545", "UTC+5:45(NPT)");
        line7.put(base + "-UTCp6", "UTC+6(BHT)");
        LinkedHashMap<String, String> line8 = new LinkedHashMap<>();
        line8.put(base + "-UTCp630", "UTC+6:30(MMT)");
        line8.put(base + "-UTCp7", "UTC+7(ICT)");
        line8.put(base + "-UTCp8", "UTC+8(CT/CST)");
        line8.put(base + "-UTCp9", "UTC+9(JST)");
        LinkedHashMap<String, String> line9 = new LinkedHashMap<>();
        line9.put(base + "-UTCp930", "UTC+9:30(ACST)");
        line9.put(base + "-UTCp10", "UTC+10(AEST)");
        line9.put(base + "-UTCp1030", "UTC+10:30(LHST)");
        line9.put(base + "-UTCp11", "UTC+11(VUT)");
        LinkedHashMap<String, String> line10 = new LinkedHashMap<>();
        line10.put(base + "-UTCp12", "UTC+12(NZST)");
        line10.put(base + "-UTCp1245", "UTC+12:45(CHAST)");
        line10.put(base + "-UTCp13", "UTC+13(PHOT)");
        line10.put(base + "-UTCp14", "UTC+14(LINT)");
        LinkedHashMap<String, String> line11 = new LinkedHashMap<>();
        line11.put("sleep-setting", "返回设置");
        ArrayList<LinkedHashMap<String, String>> innerData = new ArrayList<>(Arrays.asList(line1, line2, line3, line4, line5, line6, line7, line8, line9, line10, line11));
        return baseService.initInlineKeyboard(innerData);
    }

    private InlineKeyboardMarkup buttonLength() {
        String base = "sleep-setting-interval-length";
        LinkedHashMap<String, String> line1 = new LinkedHashMap<>();
        line1.put(base + "-1", "间隔 1 分钟");
        line1.put(base + "-5", "间隔 5 分钟");
        line1.put(base + "-10", "间隔 10 分钟");
        LinkedHashMap<String, String> line2 = new LinkedHashMap<>();
        line2.put("sleep-setting", "返回设置");
        ArrayList<LinkedHashMap<String, String>> innerData = new ArrayList<>(List.of(line1, line2));
        return baseService.initInlineKeyboard(innerData);
    }

    private InlineKeyboardMarkup buttonFrequency() {
        String base = "sleep-setting-interval-frequency";
        LinkedHashMap<String, String> line1 = new LinkedHashMap<>();
        line1.put(base + "-1", "1 次");
        line1.put(base + "-2", "2 次");
        line1.put(base + "-3", "3 次");
        LinkedHashMap<String, String> line2 = new LinkedHashMap<>();
        line2.put("sleep-setting", "返回设置");
        ArrayList<LinkedHashMap<String, String>> innerData = new ArrayList<>(List.of(line1, line2));
        return baseService.initInlineKeyboard(innerData);
    }

    private InlineKeyboardMarkup buttonTimes() {
        String base = "sleep-setting-time";
        LinkedHashMap<String, String> line1 = new LinkedHashMap<>();
        line1.put(base + "-0", "00 点");
        line1.put(base + "-1", "01 点");
        line1.put(base + "-2", "02 点");
        line1.put(base + "-3", "03 点");
        line1.put(base + "-4", "04 点");
        LinkedHashMap<String, String> line2 = new LinkedHashMap<>();
        line2.put(base + "-5", "05 点");
        line2.put(base + "-6", "06 点");
        line2.put(base + "-7", "07 点");
        line2.put(base + "-8", "08 点");
        line2.put(base + "-9", "09 点");
        LinkedHashMap<String, String> line3 = new LinkedHashMap<>();
        line3.put(base + "-10", "10 点");
        line3.put(base + "-11", "11 点");
        line3.put(base + "-12", "12 点");
        line3.put(base + "-13", "13 点");
        line3.put(base + "-14", "14 点");
        LinkedHashMap<String, String> line4 = new LinkedHashMap<>();
        line4.put(base + "-15", "15 点");
        line4.put(base + "-16", "16 点");
        line4.put(base + "-17", "17 点");
        line4.put(base + "-18", "18 点");
        line4.put(base + "-19", "19 点");
        LinkedHashMap<String, String> line5 = new LinkedHashMap<>();
        line5.put(base + "-20", "20 点");
        line5.put(base + "-21", "21 点");
        line5.put(base + "-22", "22 点");
        line5.put(base + "-23", "23 点");
        line5.put(base + "-0", "24 点");
        LinkedHashMap<String, String> line6 = new LinkedHashMap<>();
        line6.put("sleep-setting", "返回设置");
        ArrayList<LinkedHashMap<String, String>> innerData = new ArrayList<>(Arrays.asList(line1, line2, line3, line4, line5, line6));
        return baseService.initInlineKeyboard(innerData);
    }

    private InlineKeyboardMarkup buttonDuration() {
        String base = "sleep-setting-duration";
        LinkedHashMap<String, String> line1 = new LinkedHashMap<>();
        line1.put(base + "day-1", "一天");
        line1.put(base + "day-3", "三天");
        LinkedHashMap<String, String> line2 = new LinkedHashMap<>();
        line2.put(base + "week-1", "一周");
        line2.put(base + "week-2", "两周");
        LinkedHashMap<String, String> line3 = new LinkedHashMap<>();
        line3.put(base + "month-1", "一个月");
        LinkedHashMap<String, String> line6 = new LinkedHashMap<>();
        line6.put("sleep-setting", "返回设置");
        ArrayList<LinkedHashMap<String, String>> innerData = new ArrayList<>(Arrays.asList(line1, line2, line3, line6));
        return baseService.initInlineKeyboard(innerData);
    }

    private InlineKeyboardMarkup buttonChat(BotSleep sleep, Message message) {
        BotChat chat = chatService.getById(sleep.getChatId());
        String base = "sleep-setting-chat";
        LinkedHashMap<String, String> line1 = new LinkedHashMap<>();
        line1.put(base + "origin", "原位置(" + chat.getTitle() + ")");
        line1.put(base + "now", "当前位置(" + message.getChat().getTitle() + ")");
        LinkedHashMap<String, String> line6 = new LinkedHashMap<>();
        line6.put("sleep-setting", "返回设置");
        ArrayList<LinkedHashMap<String, String>> innerData = new ArrayList<>(Arrays.asList(line1, line6));
        return baseService.initInlineKeyboard(innerData);
    }

}
