package com.polycoffee.dao;

import java.util.List;

/** Khung CRUD dùng chung cho các DAO. T: kiểu entity, K: kiểu khóa chính. */
public interface CrudDAO<T, K> {
    int create(T entity);
    int update(T entity);
    int delete(K id);
    List<T> findAll();
    T findById(K id);
}
