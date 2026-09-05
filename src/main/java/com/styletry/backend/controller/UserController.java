package com.styletry.backend.controller;

import com.styletry.backend.dto.response.UserResponse;
import com.styletry.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll()
                .stream()
                .map(user -> {
                    UserResponse response = new UserResponse();
                    response.setId(user.getId());
                    response.setEmail(user.getEmail());
                    response.setFullName(user.getFullName());
                    response.setRole(user.getRole());
                    response.setCreatedAt(user.getCreatedAt());
                    return response;
                })
                .collect(Collectors.toList()));
    }
}
