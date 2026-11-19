package com.polycoffee.dao;

import com.polycoffee.entity.Bill;
import com.polycoffee.util.JdbcUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class BillDAO implements CrudDAO<Bill, Integer> {

    private static final String INSERT_SQL =
            "INSERT INTO bills(user_id, code, created_at, total, status) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL =
            "UPDATE bills SET user_id = ?, code = ?, created_at = ?, total = ?, status = ? WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM bills WHERE id = ?";
    private static final String SELECT_ALL_SQL = "SELECT * FROM bills";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM bills WHERE id = ?";

    @Override
    public int create(Bill b) {
        try {
            return JdbcUtil.executeUpdate(INSERT_SQL, b.getUserId(), b.getCode(),
                    java.sql.Timestamp.valueOf(b.getCreatedAt()), b.getTotal(), b.getStatus());
        } catch (SQLException e) { throw new RuntimeException("Thêm hóa đơn thất bại", e); }
    }

    @Override
    public int update(Bill b) {
        try {
            return JdbcUtil.executeUpdate(UPDATE_SQL, b.getUserId(), b.getCode(),
                    java.sql.Timestamp.valueOf(b.getCreatedAt()), b.getTotal(), b.getStatus(), b.getId());
        } catch (SQLException e) { throw new RuntimeException("Cập nhật hóa đơn thất bại", e); }
    }

    @Override
    public int delete(Integer id) {
        try {
            return JdbcUtil.executeUpdate(DELETE_SQL, id);
        } catch (SQLException e) { throw new RuntimeException("Xóa hóa đơn thất bại", e); }
    }

    @Override
    public List<Bill> findAll() {
        try {
            return JdbcUtil.executeQuery(SELECT_ALL_SQL, BillDAO::map);
        } catch (SQLException e) { throw new RuntimeException("Lấy danh sách hóa đơn thất bại", e); }
    }

    @Override
    public Bill findById(Integer id) {
        try {
            List<Bill> list = JdbcUtil.executeQuery(SELECT_BY_ID_SQL, BillDAO::map, id);
            return list.isEmpty() ? null : list.get(0);
        } catch (SQLException e) { throw new RuntimeException("Tìm hóa đơn thất bại", e); }
    }

    private static Bill map(ResultSet rs) throws SQLException {
        return new Bill(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("code"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getInt("total"),
                rs.getString("status"));
    }
}
