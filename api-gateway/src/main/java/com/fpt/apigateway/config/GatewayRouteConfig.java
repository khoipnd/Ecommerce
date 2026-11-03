package com.fpt.apigateway.config;

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

    // =========================
    // AUTH SERVICE - PORT 8081
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
                .before(uri("http://localhost:8081"))
                .before(removeRequestHeader("Origin"))
                .build();
    }


    // =========================
    // CATEGORY SERVICE - PORT 8082
    // =========================

    @Bean
    public RouterFunction<ServerResponse> categoryRoute() {
        return route("category-service")
                .route(
                        request ->
                                request.path().startsWith("/api/v1/categories"),
                        http()
                )
                .before(uri("http://localhost:8082"))
                .before(removeRequestHeader("Origin"))
                .build();
    }


    // =========================
    // ORDER SERVICE - PORT 8083
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
                .before(uri("http://localhost:8083"))
                .before(removeRequestHeader("Origin"))
                .build();
    }


    // =========================
    // PRODUCT SERVICE - PORT 8084
    // =========================

    @Bean
    public RouterFunction<ServerResponse> productRoute() {
        return route("product-service")
                .route(
                        request ->
                                request.path().startsWith("/api/v1/products"),
                        http()
                )
                .before(uri("http://localhost:8084"))
                .before(removeRequestHeader("Origin"))
                .build();
    }
}
