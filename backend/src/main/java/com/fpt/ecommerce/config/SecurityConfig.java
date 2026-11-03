package com.fpt.ecommerce.config;

import com.fpt.ecommerce.security.JwtAuthenticationFilter;
import com.fpt.ecommerce.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            AuthService authService
    ) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                // Cho phép FE gọi API
                .cors(cors -> {})

                // API dùng JWT nên không dùng session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // Đọc JWT từ Authorization: Bearer <token>
                .addFilterBefore(
                        new JwtAuthenticationFilter(authService),
                        UsernamePasswordAuthenticationFilter.class
                )

                // Xử lý lỗi authentication / authorization
                .exceptionHandling(errors -> errors

                        // Chưa đăng nhập
                        .authenticationEntryPoint((request, response, error) -> {
                            response.setStatus(401);
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"error\":\"Login required or session expired\"}"
                            );
                        })

                        // Không đủ quyền
                        .accessDeniedHandler((request, response, error) -> {
                            response.setStatus(403);
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"error\":\"You do not have permission for this action\"}"
                            );
                        })
                )

                .authorizeHttpRequests(auth -> auth

                        // =========================
                        // AUTH - PUBLIC
                        // =========================

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/auth/login",
                                "/api/v1/auth/register"
                        ).permitAll()

                        // =========================
                        // PRODUCT / CATEGORY - GET PUBLIC
                        // =========================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/products",
                                "/api/v1/products/**",
                                "/api/v1/categories",
                                "/api/v1/categories/**"
                        ).permitAll()

                        // =========================
                        // ADMIN - WRITE
                        // =========================

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/products",
                                "/api/v1/categories"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/products/**",
                                "/api/v1/categories/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/v1/products/**",
                                "/api/v1/categories/**",
                                "/api/v1/admin/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/products/**",
                                "/api/v1/categories/**"
                        ).hasRole("ADMIN")

                        // =========================
                        // ADMIN API
                        // =========================

                        .requestMatchers("/api/v1/admin/**")
                        .hasRole("ADMIN")

                        // =========================
                        // OTHER API
                        // =========================

                        .requestMatchers("/api/**")
                        .authenticated()

                        .anyRequest()
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173,http://localhost:4173,http://127.0.0.1:4173}")
            String allowedOrigins
    ) {

        CorsConfiguration configuration = new CorsConfiguration();

        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        if (origins.contains("*") || origins.isEmpty()) {
            configuration.setAllowedOriginPatterns(List.of("*"));
        } else {
            configuration.setAllowedOrigins(origins);
        }

        configuration.setAllowedMethods(Arrays.asList(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }
}
