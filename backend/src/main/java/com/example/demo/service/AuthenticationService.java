package com.example.demo.service;


import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.AuthResponse;

import com.example.demo.dto.RegisterRequest;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request){
        // 1. 建立 User 物件
        var user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ROLE_USER);
        // 2. 存入資料庫
        userRepository.save(user);

        // 3. 生成 Token
        var jwtToken = jwtService.generateToken(user.getUsername());
        return new AuthResponse(jwtToken);
    }

    public AuthResponse login(AuthRequest request){
        // 1. 使用 AuthenticationManager 驗證帳號密碼
        // 如果驗證失敗 (密碼錯誤)，這裡會直接拋出異常 (403/401)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        // 2. 如果程式跑到這，代表驗證成功。去撈 User 資料
        var user =  userRepository.findByUsername(request.getUsername())
                .orElseThrow();
        // 3. 生成 Token
        var jwtToken = jwtService.generateToken(user.getUsername());
        return new AuthResponse(jwtToken);
    }
}
