package com.example.photobook.service;

import com.example.photobook.dto.UserDto;
import com.example.photobook.entity.User;
import com.example.photobook.mapper.UserMapper;
import com.example.photobook.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserDto create(UserDto dto) {
        User user = userMapper.toEntity(dto);
        if (user.getIsActive() == null) {
            user.setIsActive(true);
        }
        return userMapper.toDto(userRepository.save(user));
    }

    public UserDto update(UUID id, UserDto dto) {
        User user = findByUserId(id);
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPasswordHash(dto.getPasswordHash());
        user.setAvatarUrl(dto.getAvatarUrl());
        user.setPhone(dto.getPhone());
        user.setBio(dto.getBio());
        if (dto.getIsActive() != null) {
            user.setIsActive(dto.getIsActive());
        }
        if (dto.getRoles() != null) {
            user.setRoles(dto.getRoles());
        }
        return userMapper.toDto(userRepository.save(user));
    }

    public UserDto findById(UUID id) {
        User user = findByUserId(id);
        return userMapper.toDto(user);
    }

    public List<UserDto> findAll() {
        return userMapper.toDto(userRepository.findAll());
    }

    public UserDto delete(UUID id) {
        User user = findByUserId(id);
        user.setIsActive(false);
        return userMapper.toDto(userRepository.save(user));
    }

    private User findByUserId(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("user not found"));
    }
}
