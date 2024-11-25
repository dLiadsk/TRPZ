package org.example.repository;

import org.example.model.Folder;
import org.example.repository.interfaces.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class FolderRepository implements Repository<Folder> {

    @Override
    public Folder save(Folder entity) throws SQLException {
        return null;
    }

    @Override
    public Optional<Folder> findById(Long id) throws SQLException {
        return Optional.empty();
    }

    @Override
    public List<Folder> findAll() throws SQLException {
        return null;
    }

    @Override
    public void delete(Folder entity) throws SQLException {

    }

    @Override
    public void deleteById(Long id) throws SQLException {

    }
}
