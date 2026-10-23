package com.zjnu.studybuddy.common;

public final class MaskUtil {

    private MaskUtil() {
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        int at = email.indexOf('@');
        String name = email.substring(0, at);
        if (name.length() <= 2) {
            return "*" + email.substring(at);
        }
        return name.charAt(0) + "****" + name.charAt(name.length() - 1) + email.substring(at);
    }
}
