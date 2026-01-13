package org.chemtrovina.cmtmsys.utils;

import java.text.Normalizer;

public class TextNormalizeUtils {

    // So sánh tên: bỏ dấu + lower + trim + gộp space
    public static String normalizeName(String s) {
        if (s == null) return "";
        String x = s.trim().replaceAll("\\s+", " ").toLowerCase();

        // bỏ dấu (Vietnamese)
        x = Normalizer.normalize(x, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        // chuẩn hoá đ/Đ
        x = x.replace("đ", "d");

        return x;
    }

    // Chuẩn hoá code: trim + upper + gộp space
    public static String normalizeCode(String s) {
        if (s == null) return "";
        return s.trim().replaceAll("\\s+", "").toUpperCase();
    }
}
