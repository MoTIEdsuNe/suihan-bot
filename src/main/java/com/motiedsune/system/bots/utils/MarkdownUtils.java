package com.motiedsune.system.bots.utils;

import org.springframework.stereotype.Component;

/**
 * 用途：用于格式化 MarkdownV2 数据
 *
 * @author Xander Lau
 * @date 2023-12-29 星期五
 */
@Component
public class MarkdownUtils {

    private StringBuilder builder;

    public static StringBuilder create(){
        return new StringBuilder();
    }

    public MarkdownUtils title(String input){
        this.builder.append(input);
        return this;
    }

    public MarkdownUtils text(String input){
        this.builder.append(input);
        return this;
    }
}
