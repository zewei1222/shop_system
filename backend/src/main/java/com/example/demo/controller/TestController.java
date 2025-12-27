package com.example.demo.controller;

import com.example.demo.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test/auth")
public class TestController {

    @Autowired
    private JwtService jwtService;

    @GetMapping("/get-token")
    // 修改這裡：加入 role 參數，並設定預設值
    public String getToken(
            @RequestParam String username,
            @RequestParam(defaultValue = "ROLE_USER") String role
    ) {
        // 呼叫修改後的 generateToken，把接收到的 role 傳進去
        return jwtService.generateToken(username, role);
    }

    @GetMapping("/validate-token")
    public String validateToken(@RequestParam String token, @RequestParam String username) {
        boolean isValid = jwtService.isTokenValid(token, username);
        String extractedName = jwtService.getUsername(token);
        return "Valid: " + isValid + ", User: " + extractedName;
    }
}