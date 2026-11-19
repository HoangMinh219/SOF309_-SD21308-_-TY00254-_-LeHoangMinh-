package com.polycoffee.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class JdbcUtil {
    // Thông tin kết nối đọc từ biến môi trường, không ghi mật khẩu vào mã nguồn
    private static final String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static final String DB_URL = System.getenv().getOrDefault(
            "POLYCOFFEE_DB_URL", "jdbc:sqlserver://localhost:1433;databaseName=polycoffee;encrypt=false");
    private static final String DB_USER = System.getenv().getOrDefault("POLYCOFFEE_DB_USER", "sa");
    private static final String DB_PASSWORD = System.getenv().getOrDefault("POLYCOFFEE_DB_PASSWORD", "");

    static {
        try {
            Class.forName(DRIVER); // nạp driver
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /** Mở kết nối tới SQL Server. */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    /** Tạo PreparedStatement và gán tham số (null được gán là NULL). */
    private static PreparedStatement prepare(Connection conn, String sql, Object... values) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(sql);
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null) {
                ps.setNull(i + 1, Types.NULL);
            } else {
                ps.setObject(i + 1, values[i]);
            }
        }
        return ps;
    }

    /** Thực hiện INSERT / UPDATE / DELETE, trả về số dòng bị ảnh hưởng. */
    public static int executeUpdate(String sql, Object... values) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement ps = prepare(conn, sql, values)) {
            return ps.executeUpdate();
        }
    }

    /** Thực hiện SELECT và chuyển từng dòng thành đối tượng T nhờ RowMapper. */
    public static <T> List<T> executeQuery(String sql, RowMapper<T> mapper, Object... values) throws SQLException {
        List<T> result = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = prepare(conn, sql, values);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapper.map(rs));
            }
        }
        return result;
    }
}
