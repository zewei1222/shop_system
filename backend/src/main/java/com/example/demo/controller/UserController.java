package com.example.demo.controller;

import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import com.example.demo.repository.UserRepository; // 簡單起見直接用 Repository 示範
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    // [R] 取得所有使用者 (僅限管理員)
    @GetMapping
    public ResponseEntity<?> getAllUsers(@AuthenticationPrincipal User currentUser) {
        // 簡易權限檢查
        if (currentUser.getRole() != Role.ROLE_ADMIN) {
            return ResponseEntity.status(403).body("權限不足：僅管理員可執行");
        }

        // 實務上建議回傳 UserDTO 而不是 User Entity (避免洩漏密碼)
        // 但這裡為了教學方便，直接回傳 List<User>
        // ★重要：請確保 User Entity 的 password 欄位有加 @JsonIgnore (Jackson)
        // 或是你前端不要顯示密碼欄位
        return ResponseEntity.ok(userRepository.findAll());
    }

    // [D] 刪除使用者
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        if (currentUser.getRole() != Role.ROLE_ADMIN) {
            return ResponseEntity.status(403).body("權限不足");
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}