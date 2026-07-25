// SPDX-License-Identifier: Unlicense
package hayanesuru.vincetoxicum;

import com.mojang.logging.LogUtils;
import net.minecraft.CrashReport;
import net.minecraft.SharedConstants;
import net.minecraft.server.Main;
import org.slf4j.Logger;

public class Bootstrap {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void main(String[] args) {
        try {
            SharedConstants.tryDetectVersion();
            CrashReport.preload();
            net.minecraft.server.Bootstrap.bootStrap();
            net.minecraft.server.Bootstrap.validate();
            Main.main(args);
        } catch (Exception e) {
            LOGGER.error(LogUtils.FATAL_MARKER, "Failed to start the minecraft server", e);
        } finally {
            LogUtils.INSTANCE.flush();
        }
    }
}
