package com.tiv.image.hub.util;

import cn.hutool.core.util.StrUtil;

/**
 * 颜色格式转换工具类
 */
public class ColorFormatUtils {

    /**
     * #RRGGBB
     */
    private static final String HEX_TRIPLET_COLOR_PREFIX = "#";

    /**
     * 0xRRGGBB
     */
    private static final String HEX_INTEGER_COLOR_PREFIX = "0x";

    /**
     * 将 0xRRGGBB 转换 #RRGGBB
     *
     * @param color
     * @return
     */
    public static String toTripletFormat(String color) {
        if (!isValidColor(color)) {
            return null;
        }
        // 如果已经是 # 开头,直接返回
        if (color.startsWith(HEX_TRIPLET_COLOR_PREFIX)) {
            return color;
        }
        // 替换 0x 为 #
        if (color.startsWith(HEX_INTEGER_COLOR_PREFIX)) {
            return HEX_TRIPLET_COLOR_PREFIX + color.substring(2);
        }
        return color;
    }

    /**
     * 将 #RRGGBB 转换为 0xRRGGBB
     *
     * @param color
     * @return
     */
    public static String toIntegerFormat(String color) {
        if (!isValidColor(color)) {
            return color;
        }
        // 如果已经是 0x 开头,直接返回
        if (color.startsWith(HEX_INTEGER_COLOR_PREFIX)) {
            return color;
        }
        // 替换 # 为 0x
        if (color.startsWith(HEX_TRIPLET_COLOR_PREFIX)) {
            return HEX_INTEGER_COLOR_PREFIX + color.substring(1);
        }
        return color;
    }

    /**
     * 检查颜色格式是否合法
     *
     * @param color
     * @return
     */
    public static boolean isValidColor(String color) {
        if (StrUtil.isBlank(color)) {
            return false;
        }
        // 去掉前缀后
        String hexPart;
        if (color.startsWith(HEX_TRIPLET_COLOR_PREFIX)) {
            hexPart = color.substring(1);
        } else if (color.startsWith(HEX_INTEGER_COLOR_PREFIX)) {
            hexPart = color.substring(2);
        } else {
            hexPart = color;
        }
        // 检查是否为6位十六进制
        return hexPart.length() == 6 && hexPart.matches("[0-9a-fA-F]+");
    }

}