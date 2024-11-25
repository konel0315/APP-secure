package com.example.demo; // 패키지 경로는 프로젝트 구조에 맞게 수정

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/h2-console/**").permitAll() // H2 콘솔 접근 허용
                .requestMatchers("/api/question/**").permitAll() // 사용자 등록 API 접근 허용
                .anyRequest().authenticated()
            )
            .headers(headers -> headers.frameOptions().disable()); // H2 콘솔에서 프레임 옵션 비활성화

        return http.build();
    }
}
