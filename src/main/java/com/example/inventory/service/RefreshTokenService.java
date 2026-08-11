package com.example.inventory.service;

import com.example.inventory.model.RefreshToken;

public interface RefreshTokenService {
    RefreshToken createToken(String email);

    RefreshToken validate(String token);
    
    void delete(String token);
}
