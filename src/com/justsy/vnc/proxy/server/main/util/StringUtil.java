package com.justsy.vnc.proxy.server.main.util;

public class StringUtil {
    public static boolean isEmpty(String value) {
        int strLen;
        if ((null == value) || (0 == (strLen = value.length())))
            return true;

        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(value.charAt(i)))
                return false;
        }
        return true;
    }

    public static boolean isNotEmpty(String value) {
        return !isEmpty(value);
    }
}
