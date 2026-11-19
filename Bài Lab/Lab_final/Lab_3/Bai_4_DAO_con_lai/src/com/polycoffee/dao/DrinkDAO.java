package com.polycoffee.dao;

import com.polycoffee.entity.Drink;
import com.polycoffee.util.JdbcUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DrinkDAO implements CrudDAO<Drink, Integer> {

    private static final String INSERT_SQL =
            "INSERT INTO drinks(category_id, name, price, image, description, active) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL =
            "UPDATE drinks SET category_id = ?, name = ?, price = ?, image = ?, description = ?, active = ? WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM drinks WHERE id = ?";
    private static final String SELECT_ALL_SQL = "SELECT * FROM drinks";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM drinks WHERE id = ?";

    @Override
    public int create(Drink d) {
        try {
            return JdbcUtil.executeUpdate(INSERT_SQL, d.getCategoryId(), d.getName(), d.getPrice(),
                    d.getImage(), d.getDescription(), d.isActive());
        } catch (SQLException e) { throw new RuntimeException("Thêm đồ uống thất bại", e); }
    }

    @Override
    public int update(Drink d) {
        try {
            return JdbcUtil.executeUpdate(UPDATE_SQL, d.getCategoryId(), d.getName(), d.getPrice(),
                    d.getImage(), d.getDescription(), d.isActive(), d.getId());
        } catch (SQLException e) { throw new RuntimeException("Cập nhật đồ uống thất bại", e); }
    }

    @Override
    public int delete(Integer id) {
        try {
            return JdbcUtil.executeUpdate(DELETE_SQL, id);
        } catch (SQLException e) { throw new RuntimeException("Xóa đồ uống thất bại", e); }
    }

    @Override
    public List<Drink> findAll() {
        try {
            return JdbcUtil.executeQuery(SELECT_ALL_SQL, DrinkDAO::map);
        } catch (SQLException e) { throw new RuntimeException("Lấy danh sách đồ uống thất bại", e); }
    }

    @Override
    public Drink findById(Integer id) {
        try {
            List<Drink> list = JdbcUtil.executeQuery(SELECT_BY_ID_SQL, DrinkDAO::map, id);
            return list.isEmpty() ? null : list.get(0);
        } catch (SQLException e) { throw new RuntimeException("Tìm đồ uống thất bại", e); }
    }

    private static Drink map(ResultSet rs) throws SQLException {
        return new Drink(
                rs.getInt("id"),
                rs.getInt("category_id"),
                rs.getString("name"),
                rs.getInt("price"),
                rs.getString("image"),
                rs.getString("description"),
                rs.getBoolean("active"));
    }
}
