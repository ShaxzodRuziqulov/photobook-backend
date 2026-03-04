package com.example.photobook.service;

import com.example.photobook.dto.LoginUserDto;
import com.example.photobook.dto.RefreshTokenRequestDto;
import com.example.photobook.entity.Role;
import com.example.photobook.entity.User;
import com.example.photobook.repository.UserRepository;
import com.example.photobook.response.AuthTokenResponse;
import com.example.photobook.response.AuthUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public User login(LoginUserDto dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        dto.getUsername(),
                        dto.getPassword()
                )
        );
        return userRepository.findWithRolesByUsername(dto.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
    }

    public User refresh(RefreshTokenRequestDto dto) {
        validateRefreshToken(dto);

        String username = jwtService.extractUsername(dto.getRefreshToken());
        User user = userRepository.findWithRolesByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (!jwtService.isTokenValid(dto.getRefreshToken(), user)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        return user;
    }

    public void validateRefreshToken(RefreshTokenRequestDto dto) {
        if (dto.getRefreshToken() == null || dto.getRefreshToken().isBlank()) {
            throw new IllegalArgumentException("Refresh token is required");
        }

        if (!jwtService.isRefreshToken(dto.getRefreshToken())) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
    }

    public AuthTokenResponse buildAuthTokenResponse(User authUser) {
        AuthTokenResponse response = new AuthTokenResponse();
        response.setAccessToken(jwtService.generateToken(authUser));
        response.setRefreshToken(jwtService.generateRefreshToken(authUser));
        response.setUser(toAuthUserResponse(authUser));
        return response;
    }

    public AuthUserResponse toAuthUserResponse(User user) {
        List<String> roles = user.getRoles()
                .stream()
                .map(Role::getName)
                .toList();

        AuthUserResponse userResponse = new AuthUserResponse();
        userResponse.setId(user.getId());
        userResponse.setName((user.getFirstName() + " " + user.getLastName()).trim());
        userResponse.setEmail(user.getEmail());
        userResponse.setRoles(roles);
        userResponse.setAvatarUrl(user.getAvatarUrl());
        userResponse.setPhone(user.getPhone());
        userResponse.setBio(user.getBio());
        return userResponse;
    }
}
