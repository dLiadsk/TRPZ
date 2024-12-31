package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.example.repository.entity.EmailAccountEntity;
import org.example.repository.entity.UserEntity;
import org.example.service.dto.EmailAccountDto;
import org.example.service.dto.UserCreationDto;
import org.example.service.dto.UserDto;
import org.example.service.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserDto login(String username, String password) {
        UserDto user = userMapper.mapToUserDto(userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("Username wasn't found!")));
        if (user.getPassword().equals(password)) {
            return user;
        }
        throw new IllegalArgumentException("Wrong password!");
    }

    @Transactional
    public UserDto createUser(UserCreationDto user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists!");
        }
        if (userRepository.findByPhoneNumber(user.getPhoneNumber()).isPresent()) {
            throw new IllegalArgumentException("The phone number is already taken!");
        }

        UserEntity userEntity = UserEntity.builder()
                .password(user.getPassword())
                .phoneNumber(user.getPhoneNumber())
                .username(user.getUsername())
                .build();
        userEntity.setEmailAccounts(new ArrayList<>());
        UserEntity savedEntity = userRepository.save(userEntity);
        log.info("save" + savedEntity);
        return userMapper.mapToUserDto(savedEntity);
    }

    @Transactional(readOnly = true)
    public UserDto gerUserByUsername(String username) {
        return userMapper.mapToUserDto(userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("This username doesn't exist")));
    }

    @Transactional(readOnly = true)
    public List<EmailAccountDto> gerUserEmails(String username) {
        return userMapper.mapToListDto(userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("This username doesn't exist")).getEmailAccounts());
    }
}
