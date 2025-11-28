package com.example.demo.controller;

import com.example.demo.dto.UserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private void checkAdmin(User user) {
        if (user.getRole() != Role.ROLE_ADMIN) {
            throw new RuntimeException("權限不足：僅管理員可執行");
        }
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers(@AuthenticationPrincipal User currentUser) {
        checkAdmin(currentUser);
        List<UserResponse> list = userService.getAllUsers().stream()
                .map(UserResponse::new)
                .toList();
        return ResponseEntity.ok(list);
    }

    // [C] 新增使用者
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @RequestBody UserRequest request,
            @AuthenticationPrincipal User currentUser) {
        checkAdmin(currentUser);
        return ResponseEntity.ok(new UserResponse(userService.createUser(request)));
    }

    // [U] 編輯使用者
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @RequestBody UserRequest request,
            @AuthenticationPrincipal User currentUser) {
        checkAdmin(currentUser);
        return ResponseEntity.ok(new UserResponse(userService.updateUser(id, request, currentUser)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        checkAdmin(currentUser);
        try {
            userService.deleteUser(id, currentUser);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}