package com.miras.smartclub.util;

import java.util.regex.Pattern;

public final class PasswordValidator {

    private static final Pattern UPPERCASE = Pattern.compile("[A-ZА-ЯЁ]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");

    private PasswordValidator() {}

    public static boolean isValid(String password) {
        if (password == null) return false;
        if (password.length() < 8) return false;
        if (!UPPERCASE.matcher(password).find()) return false;
        if (!DIGIT.matcher(password).find()) return false;
        return true;
    }

    public static boolean looksLikeBCryptHash(String value) {
        if (value == null) return false;
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }
}
