package com.example.demo.config;


import com.example.demo.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        // 1. 取得 Request Header 中的 Authorization 欄位
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // 2. 檢查 Header 是否合法 (必須以 "Bearer " 開頭)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // 不合法直接放行 (交給後面的 SecurityConfig 決定要不要擋)
            // 這裡不拋出異常是因為有些公開 API (如登入、註冊) 本來就不需要 Token
            filterChain.doFilter(request, response);
            return;
        }

        // 3. 提取 Token (去掉 "Bearer " 的 7 個字元)
        // 這裡的jwt就是JwtService中的token
        jwt = authHeader.substring(7);

        // 4. 從 Token 提取使用者帳號
        username = jwtService.getUsername(jwt);

        // 5. 如果有帳號，且目前 SecurityContext 沒有驗證過 (代表還沒登入)
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null){

            // 6. 從資料庫撈出 UserDetails
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 7. 驗證 Token 是否有效
            if (jwtService.isTokenValid(jwt,  userDetails.getUsername())){

                // 8. 建立驗證物件 (這就是 Spring Security 的通行證)
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                // 9. 補充請求細節 (IP, Session ID 等)
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 10. 【關鍵】將通行證放入 SecurityContextHolder
                // 這一步做完，Spring 就認為這個 Request 是「已登入」狀態
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 11. 放行，進入下一個 Filter
        filterChain.doFilter(request, response);
    }

}
