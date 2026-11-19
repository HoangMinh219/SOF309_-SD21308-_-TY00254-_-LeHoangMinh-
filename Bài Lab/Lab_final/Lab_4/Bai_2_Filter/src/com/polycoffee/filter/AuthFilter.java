package com.polycoffee.filter;

import com.polycoffee.util.AuthUtil;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * - Chưa đăng nhập → chuyển về /login.
 * - URL /manager/* chỉ cho người có quyền quản lý; người khác nhận 403.
 */
@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String path = request.getServletPath();

        if (isPublic(path)) {
            chain.doFilter(request, response);
            return;
        }
        if (!AuthUtil.isAuthenticated(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        if (path.startsWith("/manager/") && !AuthUtil.isManager(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập trang quản lý");
            return;
        }
        chain.doFilter(request, response);
    }

    /** Các URL không cần đăng nhập: trang đăng nhập/đăng xuất và tài nguyên tĩnh. */
    private boolean isPublic(String path) {
        return path.equals("/login") || path.equals("/logout")
                || path.startsWith("/assets/") || path.startsWith("/css/")
                || path.startsWith("/js/") || path.startsWith("/images/");
    }
}
