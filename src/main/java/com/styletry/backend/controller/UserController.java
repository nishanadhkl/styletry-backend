package com.styletry.backend.controller;

import com.styletry.backend.dto.response.PageResponse;
import com.styletry.backend.dto.response.UserResponse;
import com.styletry.backend.model.Order;
import com.styletry.backend.model.User;
import com.styletry.backend.repository.CartRepository;
import com.styletry.backend.repository.OrderRepository;
import com.styletry.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;

    @GetMapping
    public ResponseEntity<?> getAllUsers(@RequestParam(required = false) Integer page,
                                         @RequestParam(required = false) Integer size) {
        if (page == null && size == null) {
            return ResponseEntity.ok(userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList()));
        }

        PageRequest pageable = PageRequest.of(
                page != null ? page : 0,
                size != null ? size : 10,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return ResponseEntity.ok(PageResponse.from(userRepository.findAll(pageable).map(this::mapToResponse)));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Admin users cannot be deleted");
        }

        cartRepository.findByUserEmail(user.getEmail()).ifPresent(cartRepository::delete);

        List<Order> orders = orderRepository.findByUser(user);
        if (!orders.isEmpty()) {
            orderRepository.deleteAll(orders);
        }

        userRepository.delete(user);
        return ResponseEntity.noContent().build();
    }

    private String resolveStatus(User user) {
        if (Boolean.FALSE.equals(user.getActive())) {
            return "INACTIVE";
        }

        LocalDateTime lastSeen = user.getLastLoginAt() != null ? user.getLastLoginAt() : user.getCreatedAt();
        return lastSeen.isBefore(LocalDateTime.now().minusDays(30)) ? "INACTIVE" : "ACTIVE";
    }

    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setRole(user.getRole());
        response.setActive(user.getActive());
        response.setLastLoginAt(user.getLastLoginAt());
        response.setStatus(resolveStatus(user));
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}
