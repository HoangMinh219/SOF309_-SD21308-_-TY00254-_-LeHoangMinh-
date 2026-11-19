package com.polycoffee.util;

import com.polycoffee.entity.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class AuthUtil {
    private static final String AUTH_USER = "authUser";

    private AuthUtil() {}

    /** Lưu người dùng đã đăng nhập vào session. */
    public static void setUser(HttpServletRequest request, User user) {
        request.getSession(true).setAttribute(AUTH_USER, user);
    }

    /** Lấy người dùng đang đăng nhập, null nếu chưa đăng nhập. */
    public static User getUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (User) session.getAttribute(AUTH_USER);
    }

    public static boolean isAuthenticated(HttpServletRequest request) {
        return getUser(request) != null;
    }

    /** Đăng nhập và có quyền quản lý (role = true). */
    public static boolean isManager(HttpServletRequest request) {
        User user = getUser(request);
        return user != null && user.isRole();
    }

    /** Xóa thông tin đăng nhập (đăng xuất). */
    public static void clear(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
