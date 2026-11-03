package com.fpt.productservice.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Verify write permissions against auth-service; public reads remain available. */
@Component
public class AdminWriteFilter extends OncePerRequestFilter {
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Account(String role) {}

    private final RestClient auth;
    private static final String products = "/api/v1/products";

    @Autowired
    public AdminWriteFilter(@Value("${auth-service.url:http://localhost:8081}") String authUrl) {
        this(RestClient.builder(), authUrl);
    }

    AdminWriteFilter(RestClient.Builder builder, String authUrl) {
        this.auth = builder.baseUrl(authUrl).build();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        boolean write = method.equals("POST") || method.equals("PUT") || method.equals("PATCH") || method.equals("DELETE");
        return !write || !(path.equals(products) || path.startsWith(products + "/"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return;
        }
        try {
            Account account = auth.get().uri("/api/v1/auth/me")
                    .header("Authorization", authorization)
                    .retrieve().body(Account.class);
            if (account == null || !"ADMIN".equals(account.role())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin role required");
                return;
            }
        } catch (RestClientResponseException ex) {
            HttpStatusCode status = ex.getStatusCode();
            response.sendError(status.value() == 401 ? 401 : 503, "Authentication service error");
            return;
        } catch (RestClientException ex) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Authentication service unavailable");
            return;
        }
        chain.doFilter(request, response);
    }
}
