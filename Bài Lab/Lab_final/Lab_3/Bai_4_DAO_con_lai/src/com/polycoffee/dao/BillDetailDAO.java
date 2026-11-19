package com.polycoffee.dao;

import com.polycoffee.entity.BillDetail;
import com.polycoffee.util.JdbcUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class BillDetailDAO implements CrudDAO<BillDetail, Integer> {

    private static final String INSERT_SQL =
            "INSERT INTO bill_details(bill_id, drink_id, quantity, price) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_SQL =
            "UPDATE bill_details SET bill_id = ?, drink_id = ?, quantity = ?, price = ? WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM bill_details WHERE id = ?";
    private static final String SELECT_ALL_SQL = "SELECT * FROM bill_details";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM bill_details WHERE id = ?";

    @Override
    public int create(BillDetail d) {
        try {
            return JdbcUtil.executeUpdate(INSERT_SQL, d.getBillId(), d.getDrinkId(), d.getQuantity(), d.getPrice());
        } catch (SQLException e) { throw new RuntimeException("Thêm chi tiết hóa đơn thất bại", e); }
    }

    @Override
    public int update(BillDetail d) {
        try {
            return JdbcUtil.executeUpdate(UPDATE_SQL, d.getBillId(), d.getDrinkId(), d.getQuantity(), d.getPrice(), d.getId());
        } catch (SQLException e) { throw new RuntimeException("Cập nhật chi tiết hóa đơn thất bại", e); }
    }

    @Override
    public int delete(Integer id) {
        try {
            return JdbcUtil.executeUpdate(DELETE_SQL, id);
        } catch (SQLException e) { throw new RuntimeException("Xóa chi tiết hóa đơn thất bại", e); }
    }

    @Override
    public List<BillDetail> findAll() {
        try {
            return JdbcUtil.executeQuery(SELECT_ALL_SQL, BillDetailDAO::map);
        } catch (SQLException e) { throw new RuntimeException("Lấy danh sách chi tiết hóa đơn thất bại", e); }
    }

    @Override
    public BillDetail findById(Integer id) {
        try {
            List<BillDetail> list = JdbcUtil.executeQuery(SELECT_BY_ID_SQL, BillDetailDAO::map, id);
            return list.isEmpty() ? null : list.get(0);
        } catch (SQLException e) { throw new RuntimeException("Tìm chi tiết hóa đơn thất bại", e); }
    }

    private static BillDetail map(ResultSet rs) throws SQLException {
        return new BillDetail(
                rs.getInt("id"),
                rs.getInt("bill_id"),
                rs.getInt("drink_id"),
                rs.getInt("quantity"),
                rs.getInt("price"));
    }
}
