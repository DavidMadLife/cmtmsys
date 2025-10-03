// utils/VersionUtils.java
package org.chemtrovina.cmtmsys.utils;

import java.util.ArrayList;
import java.util.List;

public final class VersionUtils {
    private VersionUtils() {}

    // Chuẩn hoá: bỏ tiền tố V/v, thay mọi ký tự không phải [0-9a-zA-Z] bằng dấu chấm.
    private static String normalize(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.startsWith("V") || s.startsWith("v")) s = s.substring(1);
        s = s.replaceAll("[^0-9A-Za-z]+", ".");
        return s.replaceAll("\\.+", ".").replaceAll("^\\.|\\.$", "");
    }

    private static List<Object> tokenize(String s) {
        String n = normalize(s);
        if (n.isEmpty()) return List.of();
        String[] parts = n.split("\\.");
        List<Object> tokens = new ArrayList<>(parts.length);
        for (String p : parts) {
            if (p.matches("\\d+")) {
                // số nguyên
                tokens.add(Integer.parseInt(p));
            } else {
                // tách “10a” -> [10, "a"], “rc1” -> ["rc", 1] để so lexicographic hợp lý
                String[] chunks = p.split("(?<=\\d)(?=\\D)|(?<=\\D)(?=\\d)");
                for (String c : chunks) {
                    if (c.matches("\\d+")) tokens.add(Integer.parseInt(c));
                    else tokens.add(c.toLowerCase());
                }
            }
        }
        return tokens;
    }

    /** So sánh a và b: >0 nếu a>b, <0 nếu a<b, 0 nếu bằng nhau */
    public static int compare(String a, String b) {
        List<Object> A = tokenize(a);
        List<Object> B = tokenize(b);
        int n = Math.max(A.size(), B.size());
        for (int i = 0; i < n; i++) {
            Object xa = i < A.size() ? A.get(i) : 0;
            Object xb = i < B.size() ? B.get(i) : 0;
            if (xa instanceof Integer ai && xb instanceof Integer bi) {
                int cmp = Integer.compare(ai, bi);
                if (cmp != 0) return cmp;
            } else if (xa instanceof Integer ai2 && xb instanceof String) {
                // số > chữ (2 > rc)
                return 1;
            } else if (xa instanceof String && xb instanceof Integer) {
                return -1;
            } else {
                int cmp = xa.toString().compareTo(xb.toString());
                if (cmp != 0) return cmp;
            }
        }
        return 0;
    }

    public static boolean isGreater(String a, String b) { return compare(a, b) > 0; }
    public static boolean isLess(String a, String b) { return compare(a, b) < 0; }
}
