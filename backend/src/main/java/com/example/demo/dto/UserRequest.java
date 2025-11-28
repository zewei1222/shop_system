package com.example.demo.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {
    private String username;
    private String password; // 新增時必填，編輯時選填
    private String role;     // "ROLE_USER" 或 "ROLE_ADMIN"
}