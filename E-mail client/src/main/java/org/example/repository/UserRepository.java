package org.example.repository;

import org.example.model.User;
import org.example.repository.interfaces.Repository;

import java.sql.*;
import java.util.List;
import java.util.Optional;

public class UserRepository implements Repository<User> {
    @Override
    public User save(User user) throws SQLException {
        return user;
    }

    @Override
    public Optional<User> findById(Long id) throws SQLException {
        return Optional.empty();
    }

    @Override
    public List<User> findAll() throws SQLException {
        return null;
    }

    @Override
    public void delete(User entity) throws SQLException {
        deleteById(entity.getId());
    }

    @Override
    public void deleteById(Long id) throws SQLException {

    }
}
