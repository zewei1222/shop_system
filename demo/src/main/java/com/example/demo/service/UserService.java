package com.example.demo.service;

import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // 引入加密器
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public Boolean isUserTableEmpty(){
        return userRepository.count()==0;
    }
    public User createUser(String username, String password, Role role) {
        if (userRepository.findByUsername(username).isPresent()) {
            System.out.println("[Service Error]: Username '" + username + "' Already Exists]");
            return null;
        }
        String hashedPassword = passwordEncoder.encode(password);
        User newUser = new User(username, hashedPassword, role);
        return userRepository.save(newUser);
    }

    public User login(String username, String password) {
        Optional<User> useropt = userRepository.findByUsername(username);
        if(useropt.isEmpty()){
            System.out.println("[Auth]: Username '" + username + "' Not Found");
            return null;
        }

        User user = useropt.get();

        if (passwordEncoder.matches(password, user.getPassword())) {
            System.out.println("[Auth]: Welcome " + username);
            return user;
        }else {
            System.out.println("[Auth]: Wrong password");
            return null;
        }
    }
}
