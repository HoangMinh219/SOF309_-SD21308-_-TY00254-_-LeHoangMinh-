package com.polycoffee.servlet;

import com.polycoffee.dao.CategoryDAO;
import com.polycoffee.entity.Category;
import com.polycoffee.util.ParamUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Quản lý loại đồ uống (chỉ quản lý truy cập nhờ AuthFilter).
 * GET  ?action=list | new | edit&id=  | delete&id=
 * POST action=save (thêm khi id = 0, cập nhật khi id > 0)
 */
@WebServlet("/manager/category")
public class CategoryServlet extends HttpServlet {

    private static final String LIST_PAGE = "/manager/category/list.jsp";
    private static final String FORM_PAGE = "/manager/category/form.jsp";

    private final CategoryDAO dao = new CategoryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = ParamUtil.getString(req, "action", "list");
        switch (action) {
            case "new" -> req.getRequestDispatcher(FORM_PAGE).forward(req, resp);
            case "edit" -> {
                Category c = dao.findById(ParamUtil.getInt(req, "id", 0));
                if (c == null) {
                    resp.sendRedirect(req.getContextPath() + "/manager/category?error=notfound");
                    return;
                }
                req.setAttribute("category", c);
                req.getRequestDispatcher(FORM_PAGE).forward(req, resp);
            }
            case "delete" -> {
                try {
                    dao.delete(ParamUtil.getInt(req, "id", 0));
                    resp.sendRedirect(req.getContextPath() + "/manager/category?msg=deleted");
                } catch (RuntimeException e) {
                    // Loại đã có đồ uống thì khóa ngoại không cho xóa
                    resp.sendRedirect(req.getContextPath() + "/manager/category?error=delete");
                }
            }
            default -> {
                req.setAttribute("categories", dao.findAll());
                req.getRequestDispatcher(LIST_PAGE).forward(req, resp);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int id = ParamUtil.getInt(req, "id", 0);
        String name = ParamUtil.getString(req, "name", "");
        String description = ParamUtil.getString(req, "description", "");
        boolean active = ParamUtil.getBoolean(req, "active", false);

        if (name.isEmpty()) {
            Category c = new Category(id == 0 ? null : id, name, description, active, null);
            req.setAttribute("category", c);
            req.setAttribute("error", "Tên loại đồ uống không được để trống.");
            req.getRequestDispatcher(FORM_PAGE).forward(req, resp);
            return;
        }

        if (id == 0) {
            dao.create(new Category(null, name, description, active, LocalDateTime.now()));
        } else {
            Category existing = dao.findById(id);
            if (existing == null) {
                resp.sendRedirect(req.getContextPath() + "/manager/category?error=notfound");
                return;
            }
            existing.setName(name);
            existing.setDescription(description);
            existing.setActive(active);
            dao.update(existing);
        }
        resp.sendRedirect(req.getContextPath() + "/manager/category?msg=saved");
    }
}
