package com.example.photobook.service;

import com.example.photobook.dto.UserDto;
import com.example.photobook.entity.Role;
import com.example.photobook.entity.User;
import com.example.photobook.entity.enumirated.UserStatus;
import com.example.photobook.mapper.UserMapper;
import com.example.photobook.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    public UserDto create(UserDto dto) {
        User user = mapper.toEntity(dto);
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        if (user.getIsActive() == null) {
            user.setIsActive(true);
        }
        user.setUserStatus(UserStatus.ACTIVE);
        user.setRoles(resolveRolesForCreate());
        return mapper.toDto(repository.save(user));
    }

    public UserDto update(UUID id, UserDto dto) {
        User user = findByUserId(id);
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setMiddleName(dto.getMiddleName());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        user.setAvatarUrl(dto.getAvatarUrl());
        user.setPhone(dto.getPhone());
        user.setBio(dto.getBio());
        if (dto.getIsActive() != null) {
            user.setIsActive(dto.getIsActive());
        }
        return mapper.toDto(repository.save(user));
    }

    public UserDto findById(UUID id) {
        User user = findByUserId(id);
        return mapper.toDto(user);
    }

    public List<UserDto> findAll() {
        return mapper.toDto(repository.findAll());
    }

    public UserDto delete(UUID id) {
        User user = findByUserId(id);
        user.setIsActive(false);
        return mapper.toDto(repository.save(user));
    }

    public User findByUserId(UUID id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("user not found"));
    }

    public Set<Role> resolveRolesForCreate() {
        return Set.of(requireRole("ROLE_USER"));
    }

    public Role requireRole(String roleName) {
        Role role = roleService.findByName(roleName);
        if (role == null) {
            throw new IllegalArgumentException("Role not found: " + roleName);
        }
        return role;
    }
}
