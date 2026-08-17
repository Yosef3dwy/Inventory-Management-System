package com.example.inventory.dto.response;

import com.example.inventory.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponseDTO {
    private String token;
    private UserRole role;
    private Long userId;
    private String name;
    private String email;
}
