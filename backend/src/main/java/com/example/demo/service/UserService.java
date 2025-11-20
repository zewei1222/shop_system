package com.example.demo.service;

import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean isUserTableEmpty(){
        return userRepository.count() == 0;
    }

    @Transactional
    public User addUser(User user) {
        // 1. 檢查重複
        // 注意：這裡假設你的 User Model 欄位叫 name，Repository 方法叫 findByName
        if (userRepository.findByName(user.getName()).isPresent()) {
            throw new RuntimeException("[Service Error]: Username '" + user.getName() + "' Already Exists");
        }

        // 2. 加密密碼 (直接修改傳入的物件，不需要再 new 一個)
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        // 3. 儲存
        // 這裡 save 會自動處理 id (因為傳入時 id 是 null，所以是 INSERT)
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers(User currentUser) {
        if (!currentUser.getRole().equals(Role.ROLE_ADMIN)) {
            throw new RuntimeException("[Service Error]: 權限不足");
        }
        return userRepository.findAll();
    }

    @Transactional
    public void deleteUser(String userName, User currentUser) {
        if (!currentUser.getRole().equals(Role.ROLE_ADMIN)) {
            throw new RuntimeException("[Service Error]: 權限不足");
        }

        // 防止自殺 (Admin 不能刪除自己)
        if (userName.equals(currentUser.getName())) {
            throw new RuntimeException("[Service Error]: You can't delete your own account");
        }

        User user = userRepository.findByName(userName)
                .orElseThrow(() -> new RuntimeException("[Service Error]: User " + userName + " not found"));

        userRepository.delete(user);
        System.out.println("[Service]: User Deleted Successfully");
    }

    // Login 僅負責邏輯驗證，不負責 UI 輸出
    public User login(String username, String password) {
        Optional<User> userOpt = userRepository.findByName(username);

        if (userOpt.isEmpty()) {
            return null; // 帳號不存在
        }

        User user = userOpt.get();

        if (passwordEncoder.matches(password, user.getPassword())) {
            return user; // 登入成功
        } else {
            return null; // 密碼錯誤
        }
    }
}