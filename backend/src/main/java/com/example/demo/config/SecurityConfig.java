package com.example.demo.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration; // 1. 新增 import
import org.springframework.web.cors.CorsConfigurationSource; // 2. 新增 import
import org.springframework.web.cors.UrlBasedCorsConfigurationSource; // 3. 新增 import

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. 設定 CORS (跨域資源共享)
                // 這裡告訴 Spring Security 使用我們下面定義的 corsConfigurationSource
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. 關閉 CSRF
                .csrf(csrf -> csrf.disable())

                // 3. 設定權限
                .authorizeHttpRequests(auth -> auth
                        // 白名單：允許 Auth 相關、公開測試端點、以及錯誤頁面
                        // 重點：OPTIONS 請求通常會被 cors() 設定自動放行，但有時加上 HttpMethod.OPTIONS 比較保險
                        .requestMatchers("/api/auth/**", "/test/auth/**", "/error").permitAll()
                        // 其他請求一律驗證
                        .anyRequest().authenticated()
                )

                // 4. Session 無狀態
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 5. 認證提供者
                .authenticationProvider(authenticationProvider)

                // 6. 添加 JWT Filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ★★★ 新增：CORS 配置源 ★★★
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 允許的前端網址 (Vite 預設是 5173)
        // 如果你是用 3000 或其他 Port，請在這裡加上
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));

        // 允許的 HTTP 方法
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 允許的 Header (包含 Authorization)
        configuration.setAllowedHeaders(List.of("*"));

        // 是否允許攜帶憑證 (Cookies 等，雖然 JWT 主要靠 Header，但開著比較保險)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 對所有路徑套用此設定
        return source;
    }
}