package com.motiedsune.system.bots.update.command;

import com.motiedsune.system.bots.update.IUpdateCommand;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 用途：
 *
 * @author MoTIEdsuNe
 * @date 2023-12-08 星期五
 */

@Service
public class UpdateCommandGreeting implements IUpdateCommand {

    static Set<String> commands = new HashSet<>();

    @Override
    public Set<String> commands() {
        return commands;
    }

    @Override
    public Boolean consume(Update update) {
        return null;
    }
}
