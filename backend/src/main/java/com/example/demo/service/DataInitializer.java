package com.example.demo.service;

import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // 檢查資料庫是否已經有使用者
            if (userRepository.count() == 0) {
                // 如果沒人，就創建一個預設管理員
                User admin = new User();
                admin.setUsername("admin");
                // 預設密碼 123456 (記得加密)
                admin.setPassword(passwordEncoder.encode("123456"));
                admin.setRole(Role.ROLE_ADMIN);

                userRepository.save(admin);
                System.out.println("================");
                System.out.println("系統初始化完成：已建立預設管理員");
                System.out.println("帳號：admin");
                System.out.println("密碼：123456");
                System.out.println("================");
            }
        };
    }
}