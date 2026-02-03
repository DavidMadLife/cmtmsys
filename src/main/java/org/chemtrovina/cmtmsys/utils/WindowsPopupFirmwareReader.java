package org.chemtrovina.cmtmsys.utils;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WindowsPopupFirmwareReader {

    private static final Pattern FW_PATTERN =
            Pattern.compile("Firmware\\s*Version\\s*:\\s*([0-9A-Za-z._\\-]+)", Pattern.CASE_INSENSITIVE);

    // ======= Config filter =======
    private static final String TITLE_KEY = "AitUVCExtTest";
    private static final String TEXT_KEY  = "Firmware Version";

    // ==============================
    // Public APIs
    // ==============================

    /** Auto mode: find only matching popups */
    public static List<WinDef.HWND> findFirmwarePopups() {
        List<WinDef.HWND> list = new ArrayList<>();

        User32.INSTANCE.EnumWindows((hwnd, data) -> {
            if (!User32.INSTANCE.IsWindowVisible(hwnd)) return true;

            String title = safe(getWindowText(hwnd));
            if (containsIgnoreCase(title, TITLE_KEY)) {
                list.add(hwnd);
                return true;
            }

            // if title not match, try to detect by content keyword
            // (avoid heavy scan: only read child text if needed)
            String joined = safe(joinAllChildTexts(hwnd));
            if (containsIgnoreCase(joined, TEXT_KEY)) {
                list.add(hwnd);
            }

            return true;
        }, Pointer.NULL);

        return list;
    }

    /** Read firmware version from a specific HWND */
    public static String tryReadFirmwareVersion(WinDef.HWND hwnd) {
        if (hwnd == null) return null;

        String joined = joinAllChildTexts(hwnd);

        Matcher m = FW_PATTERN.matcher(joined);
        if (m.find()) return m.group(1).trim();

        // fallback: sometimes stored on window title
        String title = getWindowText(hwnd);
        m = FW_PATTERN.matcher(title == null ? "" : title);
        return m.find() ? m.group(1).trim() : null;
    }

    // ==============================
    // Internal helpers
    // ==============================

    private static String joinAllChildTexts(WinDef.HWND parent) {
        List<String> texts = new ArrayList<>();

        User32.INSTANCE.EnumChildWindows(parent, (child, data) -> {
            String t = safe(getWindowText(child)); // for Win32 controls, GetWindowText is reliable
            if (!t.isBlank()) texts.add(t.trim());
            return true;
        }, Pointer.NULL);

        return String.join("\n", texts);
    }

    private static String getWindowText(WinDef.HWND hwnd) {
        int len = User32.INSTANCE.GetWindowTextLength(hwnd);
        if (len <= 0) return "";

        char[] buffer = new char[len + 1];
        User32.INSTANCE.GetWindowText(hwnd, buffer, buffer.length);
        return Native.toString(buffer);
    }


    private static boolean containsIgnoreCase(String s, String key) {
        if (s == null || key == null) return false;
        return s.toLowerCase().contains(key.toLowerCase());
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    public static WinDef.HWND findFirstTargetPopup(String titleKey, String textKey) {
        final WinDef.HWND[] found = { null };

        User32.INSTANCE.EnumWindows((hwnd, data) -> {
            if (!User32.INSTANCE.IsWindowVisible(hwnd)) return true;

            if (isTargetPopup(hwnd, titleKey, textKey)) {
                found[0] = hwnd;
                return false;
            }
            return true;
        }, Pointer.NULL);

        return found[0];
    }

    public static boolean isTargetPopup(WinDef.HWND hwnd, String titleKey, String textKey) {
        if (hwnd == null) return false;

        String title = safe(getWindowText(hwnd));
        if (containsIgnoreCase(title, titleKey)) return true;

        // only if title not match => check content keyword
        String joined = safe(joinAllChildTexts(hwnd));
        return containsIgnoreCase(joined, textKey);
    }

}
