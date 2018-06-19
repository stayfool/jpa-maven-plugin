package io.github.stayfool.util;

import org.apache.commons.lang3.StringUtils;

/**
 * @author stay fool
 * @date 2017/8/15
 */
public final class NameUtil {
    private NameUtil() {
    }

    /**
     * 首字母大写
     *
     * @param name 待转换的字符串
     * @return 转换后的字符串
     */
    public static String capitalizeFirst(String name) {
        if (StringUtils.isNotBlank(name)) {
            char[] array = name.toCharArray();
            array[0] -= 32;
            return String.valueOf(array);
        }
        return "";
    }

    /**
     * 下划线转驼峰
     *
     * @param name 待转字符串
     * @return 转换后字符串
     */
    public static String underlineToCamel(String name) {
        // 快速检查
        if (StringUtils.isBlank(name)) {
            // 没必要转换
            return "";
        }
        StringBuilder result = new StringBuilder();
        // 用下划线将原始字符串分割
        String[] camels = name.toLowerCase().split("_");
        for (String camel : camels) {
            // 跳过原始字符串中开头、结尾的下换线或双重下划线
            if (StringUtils.isBlank(camel)) {
                continue;
            }
            // 处理真正的驼峰片段
            if (result.length() == 0) {
                // 第一个驼峰片段，全部字母都小写
                result.append(camel);
            } else {
                // 其他的驼峰片段，首字母大写
                result.append(capitalizeFirst(camel));
            }
        }
        return result.toString();
    }
}
