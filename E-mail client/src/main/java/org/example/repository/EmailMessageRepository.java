package org.example.repository;

import org.example.model.EmailMessage;
import org.example.repository.interfaces.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class EmailMessageRepository implements Repository<EmailMessage> {
    @Override
    public EmailMessage save(EmailMessage entity) throws SQLException {
        return null;
    }

    @Override
    public Optional<EmailMessage> findById(Long id) throws SQLException {
        return Optional.empty();
    }

    @Override
    public List<EmailMessage> findAll() throws SQLException {
        return null;
    }

    @Override
    public void delete(EmailMessage entity) throws SQLException {

    }

    @Override
    public void deleteById(Long id) throws SQLException {

    }
}
