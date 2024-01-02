package com.motiedsune.system.bots.utils;

import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;

/**
 * 用途：用于格式化 MarkdownV2 数据
 *
 * @author Xander Lau
 * @date 2023-12-29 星期五
 */
@Component
public class MarkdownUtils {

    private static final String[] symbols = {
            "_",
            "*",
            "[", "]",
            "(", ")",
            "{", "}",
            "~",
            "`",
            ">",
            "#",
            "+",
            "-",
            "=",
            "|",
            ".",
            "!"
    };

    private StringBuilder builder;

    public static MarkdownUtils create() {
        MarkdownUtils utils = new MarkdownUtils();
        utils.builder = new StringBuilder();
        return utils;
    }

    public static MarkdownUtils create(@NotNull StringBuilder stringBuilder) {
        MarkdownUtils utils = new MarkdownUtils();
        utils.builder = stringBuilder;
        return utils;
    }

    public MarkdownUtils bold(String input) {
        input = formatMarkdownV2(input);
        this.builder.append("*").append(input).append("*");
        return this;
    }


    public MarkdownUtils italic(String input) {
        input = formatMarkdownV2(input);
        this.builder.append("_").append(input).append("_");
        return this;
    }


    public MarkdownUtils underline(String input) {
        input = formatMarkdownV2(input);
        this.builder.append("__").append(input).append("__");
        return this;
    }


    public MarkdownUtils strikethrough(String input) {
        input = formatMarkdownV2(input);
        this.builder.append("~").append(input).append("~");
        return this;
    }

    public MarkdownUtils inlineUrl(String text, String url) {
        text = formatMarkdownV2(text);
        url = formatMarkdownV2(url);
        this.builder.append("[").append(text).append("]").append("(").append(url).append(")");
        return this;
    }

    public MarkdownUtils inlineUser(String text, Long userId) {
        text = formatMarkdownV2(text);
        this.builder.append("[").append(text).append("]").append("(tg://user?id=").append(userId).append(")");
        return this;
    }


    public MarkdownUtils codeBlock(String input) {
        input = formatMarkdownV2(input);
        this.builder.append("```\n").append(input).append("```\n");
        return this;
    }

    public MarkdownUtils codeBlock(String preFormattedName, String input) {
        input = formatMarkdownV2(input);
        this.builder.append("```").append(preFormattedName).append("\n").append(input).append("```\n");
        return this;
    }

    public MarkdownUtils text(String input) {
        input = formatMarkdownV2(input);
        this.builder.append(input);
        return this;
    }

    public MarkdownUtils textNotFormat(String input) {
        this.builder.append(input);
        return this;
    }

    public static String formatMarkdownV2(String msg) {
        for (String symbol : symbols) {
            if (msg.contains(symbol)) {
                msg = msg.replace(symbol, "\\" + symbol);
            }
        }
        return msg;
    }

    public StringBuilder build() {
        return builder;
    }

}
