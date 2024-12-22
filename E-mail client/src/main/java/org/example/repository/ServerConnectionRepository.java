package org.example.repository;

import org.example.model.ServerConnection;
import org.example.repository.interfaces.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ServerConnectionRepository implements Repository<ServerConnection> {

    @Override
    public ServerConnection save(ServerConnection entity) throws SQLException {
        return null;
    }

    @Override
    public Optional<ServerConnection> findById(Long id) throws SQLException {
        return Optional.empty();
    }

    @Override
    public List<ServerConnection> findAll() throws SQLException {
        return null;
    }

    @Override
    public void delete(ServerConnection entity) throws SQLException {

    }

    @Override
    public void deleteById(Long id) throws SQLException {

    }
}
