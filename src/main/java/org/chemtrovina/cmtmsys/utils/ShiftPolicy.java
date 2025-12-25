package org.chemtrovina.cmtmsys.utils;

import java.time.LocalTime;

public class ShiftPolicy {
    public static boolean isLateIn(String shiftName, String timeStr) {
        if (timeStr == null || timeStr.isBlank()) return false;
        LocalTime time = LocalTime.parse(timeStr);
        return switch (shiftName) {
            case "Ca 1", "HC" -> time.isAfter(LocalTime.of(8, 0));
            case "Ca 2" -> time.isAfter(LocalTime.of(20, 0));
            default -> false;
        };
    }

    public static boolean isEarlyOut(String shiftName, String timeStr) {
        if (timeStr == null || timeStr.isBlank()) return false;
        LocalTime time = LocalTime.parse(timeStr);
        return switch (shiftName) {
            case "Ca 1" -> time.isBefore(LocalTime.of(16, 0));
            case "Ca 2" -> time.isBefore(LocalTime.of(4, 0));
            case "HC" -> time.isBefore(LocalTime.of(17, 0));
            default -> false;
        };
    }
}