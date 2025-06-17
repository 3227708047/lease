package com.atguigu.lease.common.utils;

import java.util.Random;

public class CodeUtil {
    public static String getRandomCode(Integer length)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++)
        {
            Random random = new Random();
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
