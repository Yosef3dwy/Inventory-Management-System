package com.example.inventory.security;

import com.example.inventory.enums.UserRole;

public record AuthUser(String token, UserRole role, Long userId, String name, String email) {
}
