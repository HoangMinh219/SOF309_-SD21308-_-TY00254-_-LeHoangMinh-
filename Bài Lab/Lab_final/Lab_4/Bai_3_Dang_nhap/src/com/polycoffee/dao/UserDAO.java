package com.polycoffee.dao;

import com.polycoffee.entity.User;
import com.polycoffee.util.JdbcUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDAO implements CrudDAO<User, Integer> {

    private static final String INSERT_SQL =
            "INSERT INTO users(email, password, full_name, phone, role, active) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL =
            "UPDATE users SET email = ?, password = ?, full_name = ?, phone = ?, role = ?, active = ? WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM users WHERE id = ?";
    private static final String SELECT_ALL_SQL = "SELECT * FROM users";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM users WHERE id = ?";
    private static final String SELECT_BY_EMAIL_SQL = "SELECT * FROM users WHERE email = ?";

    @Override
    public int create(User u) {
        try {
            return JdbcUtil.executeUpdate(INSERT_SQL, u.getEmail(), u.getPassword(), u.getFullName(),
                    u.getPhone(), u.isRole(), u.isActive());
        } catch (SQLException e) { throw new RuntimeException("Thêm người dùng thất bại", e); }
    }

    @Override
    public int update(User u) {
        try {
            return JdbcUtil.executeUpdate(UPDATE_SQL, u.getEmail(), u.getPassword(), u.getFullName(),
                    u.getPhone(), u.isRole(), u.isActive(), u.getId());
        } catch (SQLException e) { throw new RuntimeException("Cập nhật người dùng thất bại", e); }
    }

    @Override
    public int delete(Integer id) {
        try {
            return JdbcUtil.executeUpdate(DELETE_SQL, id);
        } catch (SQLException e) { throw new RuntimeException("Xóa người dùng thất bại", e); }
    }

    @Override
    public List<User> findAll() {
        try {
            return JdbcUtil.executeQuery(SELECT_ALL_SQL, UserDAO::map);
        } catch (SQLException e) { throw new RuntimeException("Lấy danh sách người dùng thất bại", e); }
    }

    @Override
    public User findById(Integer id) {
        try {
            List<User> list = JdbcUtil.executeQuery(SELECT_BY_ID_SQL, UserDAO::map, id);
            return list.isEmpty() ? null : list.get(0);
        } catch (SQLException e) { throw new RuntimeException("Tìm người dùng thất bại", e); }
    }

    /** Lấy người dùng theo email (dùng cho đăng nhập); null nếu không tồn tại. */
    public User findByEmail(String email) {
        try {
            List<User> list = JdbcUtil.executeQuery(SELECT_BY_EMAIL_SQL, UserDAO::map, email);
            return list.isEmpty() ? null : list.get(0);
        } catch (SQLException e) { throw new RuntimeException("Tìm người dùng theo email thất bại", e); }
    }

    private static User map(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getString("full_name"),
                rs.getString("phone"),
                rs.getBoolean("role"),
                rs.getBoolean("active"));
    }
}
