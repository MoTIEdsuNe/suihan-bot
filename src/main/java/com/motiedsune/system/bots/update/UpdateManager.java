package com.motiedsune.system.bots.update;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.motiedsune.system.bots.utils.BotUtils;
import com.motiedsune.system.bots.service.IBotBaseService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用途：Update 消息管理

 * @author MoTIEdsuNe
 * @date 2023-12-08 星期五
 */
@Slf4j
@Component
public class UpdateManager {

    @Resource
    List<IUpdateCommand> commandImpls;

    Map<String, IUpdateCommand> commandMap;

    Map<String, String> commandMapping;

    @Resource
    UpdateAuthorize authorize;

    @Resource
    ObjectMapper mapper;

    @Resource
    IBotBaseService baseService;

    @PostConstruct
    public void init() {
        update();
    }

    /**
     * 用于刷新指令列表
     */
    public void update() {
        // 默认指令
        commandMap = commandImpls.stream()
                .flatMap(impl -> impl.commands().stream().map(str -> new Object[]{str, impl}))
                .collect(Collectors.toMap(d -> (String) d[0], d -> (IUpdateCommand) d[1]));
        // 指令映射
        commandMapping = commandMap.keySet().stream().collect(Collectors.toMap(d -> d, d -> d));

    }

    // 消耗指令
    public void consume(Update update) {
        try {
            String json = mapper.writeValueAsString(update);
            log.info(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        // 确认授权
        boolean confirm = authorize.confirmAuthorization(update);
        if (!confirm) return;

        if (update.hasMessage()) {
            Message message = update.getMessage();
            String text = message.getText();

            if (text != null && text.startsWith("/")) {
                // 指令列表
                List<MessageEntity> entities = message.getEntities();
                Map<String, String> commands = BotUtils.parseCommands(text);
                for (String command : commands.keySet()) {
                    // 提取指令名和参数
                    String commandName = command.substring(1);
                    IUpdateCommand func = commandMap.getOrDefault(commandName, null);
                    if (func == null) return;
                    // 执行指令
                    func.consume(update);
                }
            }
        }

        // 如果是按钮返回参数
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String data = callbackQuery.getData();
            Message message = callbackQuery.getMessage();
            Message replyToMessage = message.getReplyToMessage();
            User nowUser = callbackQuery.getFrom();
            User replyUser = replyToMessage.getFrom();
            String[] commands = data.split("-");
            if(commands.length == 0) return;

            if (!Objects.equals(nowUser.getId(), replyUser.getId())) {
                baseService.sendCallbackQuery(callbackQuery.getId(),"自己边儿玩去~，别点人家的按钮嘛！");
                return;
            }
            String commandName = commands[0];
            IUpdateCommand func = commandMap.getOrDefault(commandName, null);
            func.callbackQuery(update);
        }
    }
}
