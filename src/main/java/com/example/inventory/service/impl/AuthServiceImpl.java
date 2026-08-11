package com.example.inventory.service.impl;

import com.example.inventory.dto.request.LoginRequestDTO;
import com.example.inventory.dto.response.AuthResponseDTO;
import com.example.inventory.security.JwtService;
import com.example.inventory.service.AuthService;
import com.example.inventory.service.RefreshTokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        
        // 1. Verify credentials against the DB (this triggers CustomUserDetailsService)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Generate Tokens
        String accessToken = jwtService.generateToken(request.getEmail());
        
        String refreshToken = refreshTokenService
                .createToken(request.getEmail())
                .getToken();

        // 3. Return the payload
        return AuthResponseDTO.builder()
                .email(request.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public AuthResponseDTO refresh(String refreshToken) {
        
        // 1. Validate the refresh token in the database
        var tokenEntity = refreshTokenService.validate(refreshToken);

        // 2. Generate a new short-lived access token
        String newAccessToken = jwtService.generateToken(tokenEntity.getEmail());

        // 3. Return the new access token (while keeping the existing refresh token active)
        return AuthResponseDTO.builder()
                .email(tokenEntity.getEmail())
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // sending the same refresh token back
                .build();
    }

    @Override
    public void logout(String refreshToken) {
        log.info("Logging out and deleting refresh token for user");
        
        // Removes the refresh token from the database so it can't be used to get new access tokens
        refreshTokenService.delete(refreshToken);
    }
}