package com.vcb.tlf.utils;

import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtil {

    public static boolean isMatch(String inputString, String pattern) {
        return isMatch(inputString, pattern, 0);
    }

    public static boolean isMatch(String inputString, String pattern, int flags) {
        if (StringUtils.isEmpty(inputString) || StringUtils.isEmpty(pattern)) {
            return false;
        }
        Matcher m = Pattern.compile(pattern).matcher(inputString);
        return m.find();
    }

    public static String extract(String inputString, String pattern, int groupIndex) {
        return extract(inputString, pattern, groupIndex, 0);
    }

    public static String extract(String inputString, String pattern, int groupIndex, int flags) {
        if (!StringUtils.isEmpty(inputString) && !StringUtils.isEmpty(pattern)) {
            Matcher m = Pattern.compile(pattern).matcher(inputString);
            if (m.find()) {
                try {
                    return m.group(groupIndex);
                } catch(IllegalStateException e) {
                }
            }
        }
        return "";
    }

}
