package com.example.demo.dto;

import com.example.demo.model.Role;
import com.example.demo.model.User;

public class UserResponse {
    private Long id;
    private String username;
    private Role role;

    public UserResponse(User user){
        this.id = user.getId();
        this.username = user.getName();
        this.role = user.getRole();
    }

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public String getUsername() {return username;}
}
