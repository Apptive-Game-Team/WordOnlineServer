package com.wordonline.server.statistic.util;

public class PjpUtils {

    /**
     * Indexed scan rather than a stream: the join points this is used from carry one or two
     * arguments, and building a stream pipeline per call is more work than the search itself.
     * Hot join points should cast the fixed argument position directly instead of calling this.
     */
    public static <T> T findArg(Object[] args, Class<T> clazz) {
        for (Object arg : args) {
            if (clazz.isInstance(arg)) {
                return clazz.cast(arg);
            }
        }
        throw new IllegalArgumentException(clazz.getSimpleName() + " Not Found");
    }
}
