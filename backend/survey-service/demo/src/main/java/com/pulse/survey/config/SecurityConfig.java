package com.pulse.survey.config;

import com.pulse.survey.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Allow Swagger UI, API Docs, and Authentication APIs
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/api-docs/**",
                    "/actuator/**",
                    "/api/auth/**"
                ).permitAll()
                
                // Allow CORS Preflight OPTIONS requests
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // Question APIs
                .requestMatchers(HttpMethod.POST, "/api/questions").hasRole("GLOBAL_HR")
                .requestMatchers(HttpMethod.PUT, "/api/questions/**").hasRole("GLOBAL_HR")
                .requestMatchers(HttpMethod.DELETE, "/api/questions/**").hasRole("GLOBAL_HR")
                .requestMatchers(HttpMethod.GET, "/api/questions", "/api/questions/**").hasAnyRole("GLOBAL_HR", "HR")
                
                // Employee Active Survey API
                .requestMatchers("/api/surveys/active").hasAnyRole("EMPLOYEE", "HR", "GLOBAL_HR")
                
                // Survey lifecycle & creation APIs
                .requestMatchers(HttpMethod.POST, "/api/surveys").hasAnyRole("HR", "GLOBAL_HR")
                .requestMatchers(HttpMethod.POST, "/api/surveys/*/publish").hasAnyRole("HR", "GLOBAL_HR")
                .requestMatchers(HttpMethod.POST, "/api/surveys/*/close").hasAnyRole("HR", "GLOBAL_HR")
                .requestMatchers(HttpMethod.POST, "/api/surveys/*/questions").hasAnyRole("HR", "GLOBAL_HR")
                .requestMatchers(HttpMethod.PUT, "/api/surveys/**").hasAnyRole("HR", "GLOBAL_HR")
                .requestMatchers(HttpMethod.DELETE, "/api/surveys/**").hasAnyRole("HR", "GLOBAL_HR")
                .requestMatchers(HttpMethod.GET, "/api/surveys/**").hasAnyRole("HR", "GLOBAL_HR", "EMPLOYEE")
                
                // Employee Response APIs
                .requestMatchers(HttpMethod.POST, "/api/responses").hasRole("EMPLOYEE")
                .requestMatchers(HttpMethod.GET, "/api/responses/**").hasAnyRole("HR", "GLOBAL_HR")
                
                // Draft Questions and AI Generation APIs
                .requestMatchers(HttpMethod.POST, "/api/surveys/*/generate-ai-questions").hasAnyRole("HR", "GLOBAL_HR")
                .requestMatchers("/api/draft-questions/**").hasAnyRole("HR", "GLOBAL_HR")
                
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
