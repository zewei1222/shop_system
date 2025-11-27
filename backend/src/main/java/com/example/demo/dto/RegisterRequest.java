package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class RegisterRequest {
    @NotBlank(message = "帳號不能為空")
    @Size(min = 3, max = 20, message = "使用者名稱長度需在 3 到 20 字元之間")
    private String username;
    @NotBlank(message = "密碼不能為空")
    @Size(min = 8, max = 20, message = "密碼長度必須在 8 ~ 20 之間")
    private String password;
}