// SPDX-License-Identifier: Unlicense
package com.mojang.logging;

import hayanesuru.SimpleLogger;
import org.slf4j.Logger;
import org.slf4j.Marker;
import java.util.function.Supplier;

public class LogUtils {
    public static final SimpleLogger INSTANCE = new SimpleLogger();
    public static final Marker FATAL_MARKER = null;

    public static Logger getLogger() {
        return INSTANCE;
    }

    public static Object defer(final Supplier<Object> result) {
        class ToString {
            @Override
            public String toString() {
                return result.get().toString();
            }
        }

        return new ToString();
    }

    public static boolean isLoggerActive() {
        return true;
    }
}
