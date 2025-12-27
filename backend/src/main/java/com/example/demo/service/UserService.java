package com.example.demo.service;

import com.example.demo.dto.UserRequest;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // [C] 管理員新增使用者
    @Transactional
    public User createUser(UserRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("帳號已存在");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // 設定角色 (預設 USER)
        try {
            user.setRole(request.getRole() != null ? Role.valueOf(request.getRole()) : Role.ROLE_USER);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("無效的角色: " + request.getRole());
        }

        return userRepository.save(user);
    }

    // [U] 管理員編輯使用者
    @Transactional
    public User updateUser(Long id, UserRequest request, User currentUser) {
        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到使用者"));

        // ★ 權限防護：如果是 ADMIN，只能編輯自己，不能編輯其他 ADMIN
        if (targetUser.getRole() == Role.ROLE_ADMIN && !targetUser.getId().equals(currentUser.getId())) {
            throw new RuntimeException("權限不足：您無法編輯其他管理員的資料");
        }

        // 更新帳號 (若有變更且不重複)
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            if (!targetUser.getUsername().equals(request.getUsername()) &&
                    userRepository.findByUsername(request.getUsername()).isPresent()) {
                throw new RuntimeException("帳號已存在");
            }
            targetUser.setUsername(request.getUsername());
        }

        // 更新密碼 (只在有輸入時更新)
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            targetUser.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // 更新角色 (防止自我降級導致系統無管理員，這裡暫簡化邏輯)
        if (request.getRole() != null) {
            try {
                targetUser.setRole(Role.valueOf(request.getRole()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("無效的角色");
            }
        }

        return userRepository.save(targetUser);
    }

    // [D] 刪除使用者
    @Transactional
    public void deleteUser(Long id, User currentUser) {
        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User ID " + id + " 不存在"));

        if (targetUser.getRole() == Role.ROLE_ADMIN) {
            long adminCount = userRepository.countByRole(Role.ROLE_ADMIN);

            // admin為最高權限
            if ("admin".equals(targetUser.getUsername())) {
                throw new RuntimeException("操作被拒絕：無法刪除最高權限 Root 帳號！");
            }
            // 至少要留下一名管理員
            if (adminCount <= 1) {
                throw new RuntimeException("操作被拒絕：系統至少需要保留一位管理員！");
            }

            userRepository.delete(targetUser);
        }

        userRepository.deleteById(id);
    }
}