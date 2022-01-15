package minecraft.plugin.utils;

import static minecraft.plugin.DiscordPlugin.debugEnabled;

public class Log {
    public static void log(String message) {
        System.out.println("[Discord_Plugin] " + message);
    }

    public static void debug(String message) {
        if (debugEnabled) {
            System.out.println("[Discord_Plugin] [Debug] " + message);
        }
    }
}
