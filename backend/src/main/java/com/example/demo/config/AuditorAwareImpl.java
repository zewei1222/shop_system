package com.example.demo.config;

import com.example.demo.model.User;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 1. 如果沒有認證資訊 (例如系統啟動時)，或是匿名使用者 -> 回傳 "SYSTEM"
        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return Optional.of("SYSTEM");
        }

        // 2. 如果有登入，回傳 Username
        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            return Optional.of(((User) principal).getUsername());
        }

        return Optional.of(authentication.getName());
    }
}