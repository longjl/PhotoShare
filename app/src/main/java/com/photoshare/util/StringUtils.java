package com.photoshare.util;

import android.text.TextUtils;

/**
 * Created by yantingjun on 2014/12/4.
 */
public class StringUtils {
    public static boolean isBlank(String str) {
        return str == null || str.trim().length() <= 0;
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() <= 0;
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static boolean isNotNull(String str) {
        return str != null && str.length() > 0 && !str.equalsIgnoreCase("null");
    }

    public static String getUrlFileName(String fileUrl) {
        int index = fileUrl.lastIndexOf("/");
        System.out.println("index:" + index);
        if (index != -1) {
            String result = fileUrl.substring(index + 1);
            if (TextUtils.isEmpty(result)) {
                return MD5Util.getMD5Str(fileUrl);
            }
            return result;
        } else {
            return MD5Util.getMD5Str(fileUrl);
        }
    }
}
