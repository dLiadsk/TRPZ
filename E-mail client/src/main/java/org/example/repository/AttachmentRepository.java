package org.example.repository;

import org.example.model.Attachment;
import org.example.repository.interfaces.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class AttachmentRepository implements Repository<Attachment> {
    @Override
    public Attachment save(Attachment entity) throws SQLException {
        return null;
    }

    @Override
    public Optional<Attachment> findById(Long id) throws SQLException {
        return Optional.empty();
    }

    @Override
    public List<Attachment> findAll() throws SQLException {
        return null;
    }

    @Override
    public void delete(Attachment entity) throws SQLException {

    }

    @Override
    public void deleteById(Long id) throws SQLException {

    }
}
