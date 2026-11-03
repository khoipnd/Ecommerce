package com.fpt.authservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/** Swagger UI: thêm nút "Authorize" để dán JWT khi thử /auth/me và /users. */
@Configuration
@OpenAPIDefinition(info = @Info(title = "Auth Service API", version = "v1",
        description = "Đăng ký, đăng nhập (JWT), thông tin tài khoản, quản lý người dùng (ADMIN)"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
}
