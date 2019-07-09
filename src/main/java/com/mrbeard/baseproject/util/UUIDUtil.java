package com.mrbeard.baseproject.util;

import java.util.UUID;

/**
 * @author mrbeard
 * @Date: 2018/10/10 17:26
 * @Description:
 */
public class UUIDUtil {
    public static String getUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
