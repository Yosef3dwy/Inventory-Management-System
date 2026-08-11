package com.example.inventory.controller;

import com.example.inventory.dto.request.LoginRequestDTO;
import com.example.inventory.dto.response.AuthResponseDTO;
import com.example.inventory.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // POST: /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // POST: /api/auth/refresh
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(@RequestBody Map<String, String> body) {
        // Extracts {"refreshToken": "uuid-string"} from the JSON body
        return ResponseEntity.ok(authService.refresh(body.get("refreshToken")));
    }

    // POST: /api/auth/logout
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody Map<String, String> body) {
        authService.logout(body.get("refreshToken"));
        return ResponseEntity.ok("Logged out successfully");
    }
}