package com.example.photobook.service;

import com.example.photobook.dto.UserDto;
import com.example.photobook.dto.UserProfileUpdateDto;
import com.example.photobook.dto.UserRoleUpdateDto;
import com.example.photobook.dto.request.UserPagingRequest;
import com.example.photobook.entity.Role;
import com.example.photobook.entity.User;
import com.example.photobook.entity.enumirated.UserStatus;
import com.example.photobook.mapper.UserMapper;
import com.example.photobook.repository.UserRepository;
import com.example.photobook.service.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository repository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final CurrentUserService currentUserService;

    public UserDto create(UserDto dto) {
        validateUserForCreate(dto);
        ensureUsernameAvailable(dto.getUsername());
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
        validateUserForUpdate(dto);
        User user = findByUserId(id);
        if (!user.getUsername().equalsIgnoreCase(dto.getUsername())) {
            ensureUsernameAvailable(dto.getUsername());
        }
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setUsername(dto.getUsername());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        user.setAvatarUrl(dto.getAvatarUrl());
        user.setPhone(dto.getPhone());
        user.setProfession(dto.getProfession());
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

    public Page<UserDto> findPage(UserPagingRequest request, Pageable pageable) {
        return repository.findPage(
                request.getSearch(),
                request.getIsActive(),
                request.getRole(),
                pageable).map(mapper::toDto);
    }

    public UserDto delete(UUID id) {
        User user = findByUserId(id);
        user.setIsActive(false);
        return mapper.toDto(repository.save(user));
    }

    public UserDto updateRoles(UUID id, UserRoleUpdateDto dto) {
        if (dto.getRoleIds() == null || dto.getRoleIds().isEmpty()) {
            throw new IllegalArgumentException("role_ids is required");
        }
        User user = findByUserId(id);
        Set<Role> roles = dto.getRoleIds().stream()
                .filter(Objects::nonNull)
                .map(roleService::findByRoleId)
                .collect(java.util.stream.Collectors.toSet());
        user.setRoles(roles);
        return mapper.toDto(repository.save(user));
    }

    public UserDto getCurrentUserProfile() {
        return mapper.toDto(currentUserService.getCurrentUser());
    }

    public UserDto updateCurrentUserProfile(UserProfileUpdateDto dto) {
        validateProfileUpdate(dto);
        User user = currentUserService.getCurrentUser();
        user.setFirstName(dto.getFirstName().trim());
        user.setLastName(dto.getLastName().trim());
        user.setProfession(normalize(dto.getProfession()));
        user.setAvatarUrl(normalize(dto.getAvatarUrl()));
        user.setPhone(normalize(dto.getPhone()));
        user.setBio(normalize(dto.getBio()));
        return mapper.toDto(repository.save(user));
    }

    public User findByUserId(UUID id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("user not found"));
    }

    public Set<Role> resolveRolesForCreate() {
        return Set.of(requireRole("ROLE_OPERATOR"));
    }

    public Role requireRole(String roleName) {
        Role role = roleService.findByName(roleName);
        if (role == null) {
            throw new IllegalArgumentException("Role not found: " + roleName);
        }
        return role;
    }

    private void validateUserForCreate(UserDto dto) {
        validateCommon(dto);
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new IllegalArgumentException("password is required");
        }
    }

    private void validateUserForUpdate(UserDto dto) {
        validateCommon(dto);
    }

    private void validateCommon(UserDto dto) {
        if (dto.getUsername() == null || dto.getUsername().isBlank()) {
            throw new IllegalArgumentException("username is required");
        }
        if (dto.getFirstName() == null || dto.getFirstName().isBlank()) {
            throw new IllegalArgumentException("first_name is required");
        }
        if (dto.getLastName() == null || dto.getLastName().isBlank()) {
            throw new IllegalArgumentException("last_name is required");
        }
    }

    private void ensureUsernameAvailable(String username) {
        if (repository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("username already exists");
        }
    }

    private void validateProfileUpdate(UserProfileUpdateDto dto) {
        if (dto.getFirstName() == null || dto.getFirstName().isBlank()) {
            throw new IllegalArgumentException("first_name is required");
        }
        if (dto.getLastName() == null || dto.getLastName().isBlank()) {
            throw new IllegalArgumentException("last_name is required");
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
