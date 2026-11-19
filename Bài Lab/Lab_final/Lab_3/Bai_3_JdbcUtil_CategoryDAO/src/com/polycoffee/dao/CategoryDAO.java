package com.polycoffee.dao;

import com.polycoffee.entity.Category;
import com.polycoffee.util.JdbcUtil;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public class CategoryDAO implements CrudDAO<Category, Integer> {

    private static final String INSERT_SQL =
            "INSERT INTO categories(name, description, active, created_at) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_SQL =
            "UPDATE categories SET name = ?, description = ?, active = ? WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM categories WHERE id = ?";
    private static final String SELECT_ALL_SQL = "SELECT * FROM categories";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM categories WHERE id = ?";

    @Override
    public int create(Category c) {
        try {
            LocalDateTime now = c.getCreatedAt() != null ? c.getCreatedAt() : LocalDateTime.now();
            return JdbcUtil.executeUpdate(INSERT_SQL, c.getName(), c.getDescription(), c.isActive(), Timestamp.valueOf(now));
        } catch (SQLException e) {
            throw new RuntimeException("Thêm loại đồ uống thất bại", e);
        }
    }

    @Override
    public int update(Category c) {
        try {
            return JdbcUtil.executeUpdate(UPDATE_SQL, c.getName(), c.getDescription(), c.isActive(), c.getId());
        } catch (SQLException e) {
            throw new RuntimeException("Cập nhật loại đồ uống thất bại", e);
        }
    }

    @Override
    public int delete(Integer id) {
        try {
            return JdbcUtil.executeUpdate(DELETE_SQL, id);
        } catch (SQLException e) {
            throw new RuntimeException("Xóa loại đồ uống thất bại", e);
        }
    }

    @Override
    public List<Category> findAll() {
        try {
            return JdbcUtil.executeQuery(SELECT_ALL_SQL, CategoryDAO::map);
        } catch (SQLException e) {
            throw new RuntimeException("Lấy danh sách loại đồ uống thất bại", e);
        }
    }

    @Override
    public Category findById(Integer id) {
        try {
            List<Category> list = JdbcUtil.executeQuery(SELECT_BY_ID_SQL, CategoryDAO::map, id);
            return list.isEmpty() ? null : list.get(0);
        } catch (SQLException e) {
            throw new RuntimeException("Tìm loại đồ uống thất bại", e);
        }
    }

    private static Category map(java.sql.ResultSet rs) throws SQLException {
        return new Category(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getBoolean("active"),
                rs.getTimestamp("created_at").toLocalDateTime());
    }
}
