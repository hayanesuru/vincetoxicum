// SPDX-License-Identifier: Unlicense
package com.mojang.jtracy;

import org.jspecify.annotations.NullMarked;

@NullMarked
public final class TracyClient {

    public static Zone beginZone(String ignored1, boolean ignored2) {
        return new Zone();
    }

    public static Plot createPlot(String ignored) {
        return new Plot();
    }

    public static DiscontinuousFrame createDiscontinuousFrame(String ignored) {
        return new DiscontinuousFrame();
    }

    public static void setThreadName(String ignored, int ignoredI) {
    }

    public static boolean isAvailable() {
        return false;
    }

    public static Zone beginZone(String ignored1, String ignored2, String ignored3, int ignored4) {
        return new Zone();
    }
}
