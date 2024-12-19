package org.example.repository.interfaces;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface Repository<T> {
    T save(T entity) throws SQLException;
    Optional<T> findById(Long id) throws SQLException;
    List<T> findAll() throws SQLException;
    void delete(T entity) throws SQLException;
    void deleteById(Long id) throws SQLException;
}