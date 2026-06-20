package com.styletry.backend.service;

import com.styletry.backend.dto.request.LoginRequest;
import com.styletry.backend.dto.request.RegisterRequest;
import com.styletry.backend.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
