package org.example.repository;

import org.example.model.EmailAccount;
import org.example.repository.interfaces.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class EmailAccountRepository implements Repository<EmailAccount> {
    @Override
    public EmailAccount save(EmailAccount entity) throws SQLException {
        return null;
    }

    @Override
    public Optional<EmailAccount> findById(Long id) throws SQLException {
        return Optional.empty();
    }

    @Override
    public List<EmailAccount> findAll() throws SQLException {
        return null;
    }

    @Override
    public void delete(EmailAccount entity) throws SQLException {

    }

    @Override
    public void deleteById(Long id) throws SQLException {

    }
}
