package com.styletry.backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String email;
    private String fullName;
    private String role;
    private LocalDateTime createdAt;
}
