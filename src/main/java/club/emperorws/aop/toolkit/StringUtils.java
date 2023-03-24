package club.emperorws.aop.toolkit;

import cn.hutool.core.util.StrUtil;

/**
 * 字符串工具类
 *
 * @author: EmperorWS
 * @date: 2023/3/17 22:29
 * @description: StringUtils: 字符串工具类
 */
public class StringUtils {

    /**
     * 星号（*）模糊匹配转正则表达式
     *
     * @param srcRegex 星号（*）模糊匹配原始表达式
     * @return 转义后的正则表达式
     */
    public static String escapeRegex(String srcRegex) {
        return "^" + srcRegex
                .replaceAll("\\.", "\\\\.")
                .replaceAll("\\(", "\\\\(")
                .replaceAll("\\)", "\\\\)")
                .replaceAll("\\*", ".*?") + "$";
    }
}
