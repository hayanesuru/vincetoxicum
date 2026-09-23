// SPDX-License-Identifier: Unlicense
package com.mojang.logging;

import co.hayane.SimpleLogger;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.Marker;
import java.util.function.Supplier;

@NullMarked
public final class LogUtils {
    public static final SimpleLogger INSTANCE = new SimpleLogger();
    @Nullable
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
