package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "USERS")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", columnDefinition = "INTEGER", nullable = false, unique = true)
    private Long id;
    @Column(name = "NAME", columnDefinition = "TEXT", nullable = false, unique = true)
    private String name;
    @Column(name = "PASSWORD", columnDefinition = "TEXT", nullable = false)
    private String password;
    @Enumerated(EnumType.STRING)
    @Column(name = "ROLE", columnDefinition = "TEXT", nullable = false)
    private Role role;

//    private String FirstName;
//    private String LastName;
//    private String Email;
//    private String phoneNumber;
//    private String address;
}
