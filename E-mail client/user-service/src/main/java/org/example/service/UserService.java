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
import org.example.util.AES;
import org.example.util.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AES aes;
    private final JwtUtil jwtUtil;

    @Transactional(readOnly = true)
    public UserDto login(String usernameEnc, String passwordEnc, String key) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(key);
        SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        String username = aes.decrypt(usernameEnc, secretKey);
        String jwt = jwtUtil.generateToken(username);
        UserDto user = userMapper.mapToUserDto(userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("Username wasn't found!")), secretKey, jwt);
        if (user.getPassword().equals(passwordEnc)) {
            return user;
        }
        throw new IllegalArgumentException("Wrong password!");
    }

    @Transactional
    public UserDto createUser(UserCreationDto user) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(user.getKey());
        SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        String username = aes.decrypt(user.getUsername(), secretKey);
        String password = aes.decrypt(user.getPassword(), secretKey);
        String phoneNumber = aes.decrypt(user.getPhoneNumber(), secretKey);
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists!");
        }
        if (userRepository.findByPhoneNumber(phoneNumber).isPresent()) {
            throw new IllegalArgumentException("The phone number is already taken!");
        }

        UserEntity userEntity = UserEntity.builder()
                .password(password)
                .phoneNumber(phoneNumber)
                .username(username)
                .build();
        userEntity.setEmailAccounts(new ArrayList<>());
        UserEntity savedEntity = userRepository.save(userEntity);
        log.info("save" + savedEntity);
        String jwt = jwtUtil.generateToken(username);
        UserDto userDto = userMapper.mapToUserDto(savedEntity, secretKey, jwt);
        return userDto;
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
