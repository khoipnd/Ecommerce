package com.fpt.order.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtService jwtService
    ) throws Exception {

        http
                // REST API dùng JWT nên không cần CSRF
                .csrf(csrf -> csrf.disable())

                // Không tạo session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Phân quyền endpoint
                .authorizeHttpRequests(auth -> auth

                        // Swagger / OpenAPI được phép truy cập không cần token
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Health check
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/health/**"
                        ).permitAll()

                        // API dành riêng cho ADMIN
                        .requestMatchers(
                                "/api/v1/admin/**"
                        ).hasRole("ADMIN")

                        // API Order bắt buộc phải đăng nhập
                        .requestMatchers(
                                "/api/v1/orders",
                                "/api/v1/orders/**"
                        ).authenticated()

                        // Những endpoint khác không khai báo thì chặn
                        .anyRequest().denyAll()
                )

                // Xử lý lỗi authentication / authorization
                .exceptionHandling(exception -> exception

                        // Chưa đăng nhập / token không hợp lệ
                        .authenticationEntryPoint((request, response, ex) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");

                            response.getWriter().write(
                                    "{\"error\":\"Authentication required\"}"
                            );
                        })

                        // Đã đăng nhập nhưng không đủ quyền
                        .accessDeniedHandler((request, response, ex) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");

                            response.getWriter().write(
                                    "{\"error\":\"Access denied\"}"
                            );
                        })
                )

                // JWT filter chạy trước UsernamePasswordAuthenticationFilter
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtService),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}