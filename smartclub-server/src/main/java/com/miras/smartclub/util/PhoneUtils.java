package com.miras.smartclub.util;

public final class PhoneUtils {

    private PhoneUtils() {}

    public static String normalize(String raw) {
        if (raw == null) return null;
        String digits = raw.replaceAll("\\D+", "");
        if (digits.length() == 10) {
            return "7" + digits;
        }
        if (digits.length() == 11) {
            if (digits.startsWith("8")) {
                return "7" + digits.substring(1);
            }
            return digits;
        }
        return digits;
    }

    public static String formatForDisplay(String raw) {
        String norm = normalize(raw);
        if (norm == null || norm.length() != 11) return raw;
        String d = norm.substring(1);
        String a = d.substring(0, 3);
        String b = d.substring(3, 6);
        String c = d.substring(6, 8);
        String e = d.substring(8, 10);
        return "+7 (" + a + ") " + b + "-" + c + "-" + e;
    }
}
