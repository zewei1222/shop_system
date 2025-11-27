package com.example.demo.dto;

import com.example.demo.model.User;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UserResponse {
    private Long id;
    private String username;
    private String role;

    public UserResponse(User user){
        this.id = user.getId();
        this.username = user.getUsername();
        this.role = (user.getRole() != null) ? user.getRole().name() : null;
    }
}
