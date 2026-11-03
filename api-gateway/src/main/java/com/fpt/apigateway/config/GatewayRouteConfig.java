package com.fpt.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.removeRequestHeader;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

@Configuration
public class GatewayRouteConfig {

    @Value("${auth-service.url:http://localhost:8081}")
    private String authServiceUrl;

    @Value("${category-service.url:http://localhost:8082}")
    private String categoryServiceUrl;

    @Value("${order-service.url:http://localhost:8083}")
    private String orderServiceUrl;

    @Value("${product-service.url:http://localhost:8084}")
    private String productServiceUrl;

    // =========================
    // AUTH SERVICE
    // =========================
    @Bean
    public RouterFunction<ServerResponse> authRoute() {
        return route("auth-service")
                .route(
                        request ->
                                request.path().startsWith("/api/v1/auth")
                                        || request.path().startsWith("/api/v1/users"),
                        http()
                )
                .before(uri(authServiceUrl))
                .before(removeRequestHeader("Origin"))
                .build();
    }

    // =========================
    // CATEGORY SERVICE
    // =========================
    @Bean
    public RouterFunction<ServerResponse> categoryRoute() {
        return route("category-service")
                .route(
                        request ->
                                request.path().startsWith("/api/v1/categories"),
                        http()
                )
                .before(uri(categoryServiceUrl))
                .before(removeRequestHeader("Origin"))
                .build();
    }

    // =========================
    // ORDER SERVICE
    // =========================
    @Bean
    public RouterFunction<ServerResponse> orderRoute() {
        return route("order-service")
                .route(
                        request ->
                                request.path().startsWith("/api/v1/orders")
                                        || request.path().startsWith("/api/v1/admin/orders"),
                        http()
                )
                .before(uri(orderServiceUrl))
                .before(removeRequestHeader("Origin"))
                .build();
    }

    // =========================
    // PRODUCT SERVICE
    // =========================
    @Bean
    public RouterFunction<ServerResponse> productRoute() {
        return route("product-service")
                .route(
                        request ->
                                request.path().startsWith("/api/v1/products"),
                        http()
                )
                .before(uri(productServiceUrl))
                .before(removeRequestHeader("Origin"))
                .build();
    }
}
