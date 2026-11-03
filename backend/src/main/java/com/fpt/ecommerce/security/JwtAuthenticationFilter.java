package com.fpt.ecommerce.security;

import com.fpt.ecommerce.dto.AccountInfo;
import com.fpt.ecommerce.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final AuthService authService;

    public JwtAuthenticationFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().equals("/api/v1/auth/login")
                || request.getServletPath().equals("/api/v1/auth/register");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                AccountInfo account = authService.currentAccount(header); // Kiểm tra chữ ký/hạn JWT và đọc quyền hiện tại từ database.
                var authentication = new UsernamePasswordAuthenticationToken(account.username(), null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + account.role())));
                SecurityContextHolder.getContext().setAuthentication(authentication); // OrderController nhận username thật qua Authentication.
            } catch (ResponseStatusException error) {
                response.setStatus(401);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Invalid or expired token\"}");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
