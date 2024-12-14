package org.example.service;

import org.example.model.User;
import org.example.repository.UserRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;

public class UserService {
    private final UserRepository userRepository;


    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public User createUser(User user) throws SQLException {
        user.setId(generateUserId());
        user.setEmailAccounts(new ArrayList<>());
        if (findUserByUsername(user.getUsername()) != null) {
            throw new IllegalArgumentException("Username already exists!");
        }
        if (findUserByPhoneNumber(user.getPhoneNumber()) != null) {
            throw new IllegalArgumentException("The phone number is already taken!");
        }
        return userRepository.save(user);
    }
    public User editUser(User userEdited) throws SQLException {
        Optional<User> optionalUser = userRepository.findById(userEdited.getId());
        if (optionalUser.isPresent())  {
            User user = optionalUser.get();
            if (userEdited.getUsername() != null){
                if (findUserByUsername(user.getUsername()) != null) {
                    throw new IllegalArgumentException("Username already exists!");
                }
                user.setUsername(user.getUsername());
            }
            if (userEdited.getPhoneNumber() != null){
                if (findUserByPhoneNumber(user.getPhoneNumber()) != null) {
                    throw new IllegalArgumentException("The phone number is already taken!");
                }
                user.setPhoneNumber(user.getPhoneNumber());
            }
            return userRepository.save(user);
        }
       throw new IllegalArgumentException("This ID doesn't exists");
    }
    public User changePassword(User userWithOldPassword, String oldPassword) throws SQLException {
        Optional<User> optionalUser = userRepository.findById(userWithOldPassword.getId());
        if (optionalUser.isPresent())  {
            User user = optionalUser.get();
            if (user.getPassword().equals(oldPassword)){
                user.setPassword(userWithOldPassword.getPassword());
                return userRepository.save(user);
            }
            throw new IllegalArgumentException("You entered wrong old password");
        }
        throw new IllegalArgumentException("This ID doesn't exists");
    }
    private Long generateUserId() throws SQLException {
        return (long) (userRepository.findAll().size() + 1);
    }
    private User findUserByUsername(String username) throws SQLException {
        return userRepository.findAll().stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }
    private User findUserByPhoneNumber(String phoneNumber) throws SQLException {
        return userRepository.findAll().stream()
                .filter(user -> user.getPhoneNumber().equals(phoneNumber))
                .findFirst()
                .orElse(null);
    }
}
