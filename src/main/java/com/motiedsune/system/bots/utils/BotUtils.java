package com.motiedsune.system.bots.utils;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 用途：
 *
 * @author MoTIEdsuNe
 * @date 2023-12-21 星期四
 */
@Component
@Slf4j
public class BotUtils {

    @NotNull
    public static String getCommand(String str) {
        String pattern = "/(\\w+)";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(str);
        return matcher.find() ? matcher.group(1) : "";
    }


    public static Map<String, String> parseCommands(String text) {

        text = text.replaceAll("/([^/@\\s]+)@[^/\\s]+", "/$1");
        log.info(text);
        char[] array = text.toCharArray();
        Map<String, String> commands = new LinkedHashMap<>();
        String command = null;
        String argument = null;
        List<Character> commandChars = new LinkedList<>();
        List<Character> argumentChars = new LinkedList<>();
        boolean hasCommand = false;
        for (int i = 0; i < array.length; i++) {
            char self = array[i];
            // 如果第一个是 / 或者内部有 “ /” (空格 + /)
            if (self == '/' && (i == 0 || array[i - 1] == ' ')) {
                commandChars.clear();
                hasCommand = true;
                // 补参数
                if (command != null) {
                    argument = argumentChars.stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining());
                    commands.put(command, argument);
                    log.info("command: {} , argument: {}", command, argument);
                    // 补完清空
                    argumentChars.clear();
                }
            }

            if (hasCommand) {
                // 遇到截止符号，新增命令
                if (self == ' ' || self == '@') {
                    hasCommand = false;
                    command = commandChars.stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining());
                    commandChars.clear();
                }
                // 新增字符
                commandChars.add(self);
            } else {
                argumentChars.add(self);
            }
            // 如果不是指令
        }
        // 处理最后一条，如果这俩任意一个不是空的
        if (!commandChars.isEmpty() || !argumentChars.isEmpty()) {
            if (hasCommand) {
                command = commandChars.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining());
                commands.put(command, null);
            } else {
                argument = argumentChars.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining());
                commands.put(command, argument);
            }
            log.info("command: {} , argument: {}", command, argument);
        }
        return commands;
    }
}

