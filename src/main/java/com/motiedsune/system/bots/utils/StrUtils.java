package com.motiedsune.system.bots.utils;

/**
 * 用途：
 *
 * @author Xander Lau
 * @date 2023-12-29 星期五
 */
public class StrUtils {

    // 判断是正整数
    public static boolean isPositiveInteger(String str) {
        // 遍历字符串的每个字符
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                // 如果字符不是数字或者是零，则不是正整数
                return false;
            }
        }
        return true;
    }
}
