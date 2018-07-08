package com.seckill.miaosha.util;

/**
 * Created by syw on 2018/6/20.
 */


import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 工具类： 1.验证手机号是否正确
 *
 */
public class VaildataUtil {

    //Pattern 类用于创建一个匹配模式 联合Matcher 类使用 达到字符串匹配的效果
    private static final Pattern PATTERN = Pattern.compile("1\\d{10}");
    /**验证手机号是否正确
     * @param input
     * @return
     */
    public static boolean isMobile(String input) {
        if (input.isEmpty()) {
            return false;
        }
        Matcher match = PATTERN.matcher(input);//Pattern 返回一个 matcher 对象
        return match.matches();//matches 方法 返回值boolean 全字符匹配
    }
}
