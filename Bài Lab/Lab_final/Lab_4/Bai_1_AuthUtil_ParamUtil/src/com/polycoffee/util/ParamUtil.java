package com.polycoffee.util;

import jakarta.servlet.http.HttpServletRequest;

public class ParamUtil {
    private ParamUtil() {}

    /** Đọc tham số kiểu int; trả về defaultValue nếu thiếu hoặc sai định dạng. */
    public static int getInt(HttpServletRequest request, String name, int defaultValue) {
        String value = request.getParameter(name);
        if (value == null || value.isBlank()) return defaultValue;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /** Đọc tham số chuỗi (đã cắt khoảng trắng); trả về defaultValue nếu thiếu. */
    public static String getString(HttpServletRequest request, String name, String defaultValue) {
        String value = request.getParameter(name);
        return value == null ? defaultValue : value.trim();
    }

    /** Đọc tham số kiểu boolean: checkbox gửi "on" hoặc "true" thì là true. */
    public static boolean getBoolean(HttpServletRequest request, String name, boolean defaultValue) {
        String value = request.getParameter(name);
        if (value == null) return defaultValue;
        String v = value.trim().toLowerCase();
        return v.equals("on") || v.equals("true") || v.equals("1");
    }
}
