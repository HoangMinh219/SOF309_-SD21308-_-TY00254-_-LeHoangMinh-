package com.polycoffee.servlet;

import com.polycoffee.dao.UserDAO;
import com.polycoffee.entity.User;
import com.polycoffee.util.AuthUtil;
import com.polycoffee.util.ParamUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** /login: hiển thị form và xử lý đăng nhập. /logout: đăng xuất. */
@WebServlet({"/login", "/logout"})
public class AuthServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("/logout".equals(req.getServletPath())) {
            AuthUtil.clear(req);
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = ParamUtil.getString(req, "email", "").toLowerCase();
        String password = ParamUtil.getString(req, "password", "");

        User user = userDAO.findByEmail(email);
        // Mật khẩu so sánh trực tiếp theo đề bài; hệ thống thật nên lưu mật khẩu đã băm.
        if (user == null || !user.isActive() || !user.getPassword().equals(password)) {
            req.setAttribute("error", "Email hoặc mật khẩu không đúng.");
            req.setAttribute("email", email);
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

        AuthUtil.setUser(req, user);
        resp.sendRedirect(req.getContextPath() + (user.isRole() ? "/manager/category" : "/login"));
    }
}
