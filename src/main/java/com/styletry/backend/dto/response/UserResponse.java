package com.styletry.backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String email;
    private String fullName;
    private String role;
    private Boolean active;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}
