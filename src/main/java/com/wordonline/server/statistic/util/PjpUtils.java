package com.wordonline.server.statistic.util;

import java.util.Arrays;

public class PjpUtils {

    public static  <T> T findArg(Object[] args, Class<T> clazz) {
        return Arrays.stream(args)
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(clazz.getSimpleName() + " Not Found"));
    }
}
