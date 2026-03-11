package com.example.photobook.controller;

import com.example.photobook.dto.LoginUserDto;
import com.example.photobook.dto.RefreshTokenRequestDto;
import com.example.photobook.entity.User;
import com.example.photobook.response.AuthTokenResponse;
import com.example.photobook.response.AuthUserResponse;
import com.example.photobook.response.TokenPairResponse;
import com.example.photobook.service.AuthenticationService;
import com.example.photobook.service.JwtService;
import com.example.photobook.service.security.CurrentUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final JwtService jwtService;
    private final AuthenticationService authService;
    private final CurrentUserService currentUserService;

    @PostMapping("/login")
    public ResponseEntity<AuthTokenResponse> login(@RequestBody LoginUserDto dto) {
        User authUser = authService.login(dto);
        return ResponseEntity.ok(authService.buildAuthTokenResponse(authUser));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenPairResponse> refresh(@RequestBody RefreshTokenRequestDto dto) {
        User authUser = authService.refresh(dto);
        TokenPairResponse response = new TokenPairResponse();
        response.setAccessToken(jwtService.generateToken(authUser));
        response.setRefreshToken(jwtService.generateRefreshToken(authUser));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<AuthUserResponse> authenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        User currentUser = currentUserService.getCurrentUser();
        return ResponseEntity.ok(authService.toAuthUserResponse(currentUser));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestBody RefreshTokenRequestDto dto,
            HttpServletRequest request,
            HttpServletResponse response) {
        authService.validateRefreshToken(dto);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
