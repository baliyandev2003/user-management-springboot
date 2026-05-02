package com.example.baliyan.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Allow all static resources
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/static/**", "/webjars/**", "/uploads/**").permitAll()
                        // Allow public pages
                        .requestMatchers(
                                "/",
                                "/select-role",
                                "/auth/role-selection",
                                "/login",
                                "/signup",
                                "/forgot-password",
                                "/verify-otp",
                                "/reset-password",
                                "/access-denied",
                                "/error"
                        ).permitAll()
                        // Admin endpoints
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        // User endpoints
                        .requestMatchers("/user/**").hasRole("USER")
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/login-success", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/access-denied")
                )
                // For development - disable CSRF
//                .csrf();
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}