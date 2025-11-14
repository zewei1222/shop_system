package com.example.demo.model;

import jakarta.persistence.*; // 引入所有 JPA 註解

@Entity
@Table(name = "STUFF_USERS")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Long userId;
    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false)
    private String password;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

//    private String FirstName;
//    private String LastName;
//    private String Email;
//    private String phoneNumber;
//    private String address;
    public User (){}

    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public Role getRole() {
        return role;
    }

    public void setUserId() {
        this.userId = userId;
    }
    public void setUsername() {
        this.username = username;
    }
    public void setPassword() {
        this.password = password;
    }
    public void setRole() {
        this.role = role;
    }
}
