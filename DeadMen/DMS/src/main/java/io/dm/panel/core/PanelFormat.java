package io.dm.panel.core;

import io.dm.Server;

import java.text.DecimalFormat;

/** Small formatting helpers shared across panel tabs. */
public final class PanelFormat {

    private static final DecimalFormat COMMAS = new DecimalFormat("#,###");

    private PanelFormat() {}

    /** e.g. 1234567 -> "1,234,567". */
    public static String number(long value) {
        return COMMAS.format(value);
    }

    /** Milliseconds -> "1d 02h 03m 04s" (drops leading zero units). */
    public static String durationMs(long ms) {
        if (ms < 0) {
            return "--";
        }
        long totalSeconds = ms / 1000L;
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (days > 0 || hours > 0) sb.append(String.format("%02dh ", hours));
        sb.append(String.format("%02dm %02ds", minutes, seconds));
        return sb.toString();
    }

    /** Player playtime is stored in game ticks; convert to a friendly duration. */
    public static String playTime(long ticks) {
        return durationMs(ticks * Server.tickMs());
    }
}
