package com.polycoffee.util;

import java.sql.ResultSet;
import java.sql.SQLException;

/** Chuyển một dòng ResultSet thành đối tượng T. */
@FunctionalInterface
public interface RowMapper<T> {
    T map(ResultSet rs) throws SQLException;
}
