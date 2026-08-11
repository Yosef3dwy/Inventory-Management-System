package com.example.inventory.service;

import com.example.inventory.dto.request.LoginRequestDTO;
import com.example.inventory.dto.response.AuthResponseDTO;

public interface AuthService {
    
    AuthResponseDTO login(LoginRequestDTO request);
    
    AuthResponseDTO refresh(String refreshToken);
    
    void logout(String refreshToken);
}