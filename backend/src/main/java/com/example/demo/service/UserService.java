package com.example.demo.service;

import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // [R] 取得所有使用者 (給管理員看的)
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // [D] 刪除使用者
    @Transactional
    public void deleteUser(Long id) {
        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User ID " + id + " 不存在"));

        // 保護機制 1: 不能刪除自己 (通常由前端擋，或後端檢查 currentUser.id != id)
        // 這裡暫時省略，專注於保護 Root

        // 保護機制 2: 不能刪除 ID 為 1 的初始管理員
        // (假設你的第一個註冊者是 Root)
        if (targetUser.getId() == 1L) {
            throw new RuntimeException("無法刪除初始最高權限管理員 (Root)");
        }

        // 保護機制 3: 確保系統至少有一個 Admin
        if (targetUser.getRole() == Role.ROLE_ADMIN) {
            long adminCount = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == Role.ROLE_ADMIN)
                    .count();

            if (adminCount <= 1) {
                throw new RuntimeException("無法刪除：系統至少需要保留一位管理員");
            }
        }

        userRepository.deleteById(id);
    }

    // 原本的 login 和 createUser 全部移除！
    // 這些職責已經移交給 AuthenticationService 了
}