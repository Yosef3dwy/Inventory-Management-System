package com.example.inventory.service.impl;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.inventory.model.RefreshToken;
import com.example.inventory.repository.RefreshTokenRepository;
import com.example.inventory.service.RefreshTokenService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository repository;

    @Override
    public RefreshToken createToken(String email) {
        
        RefreshToken token = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .email(email)
                .expiryDate(Instant.now().plusSeconds(7 * 24 * 60 * 60)) // 7 days
                .build();
                
        return repository.save(token);
    }

    @Override
    public RefreshToken validate(String token) {
        
        RefreshToken refreshToken = repository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            repository.delete(refreshToken);
            throw new RuntimeException("Refresh token expired");
        }

        return refreshToken;
    }
    
    @Override
    @Transactional
    public void delete(String token) {
        repository.deleteByToken(token);
    }
}
