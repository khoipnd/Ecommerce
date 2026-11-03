package com.fpt.category.client;

import com.fpt.category.exception.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Gọi sang product-service để biết category còn sản phẩm hay không. */
@Component
public class ProductClient {

    private final RestClient rest;

    public ProductClient(@Value("${product-service.url}") String baseUrl,
                         @Value("${internal.api-key}") String apiKey) {
        this.rest = RestClient.builder().baseUrl(baseUrl).defaultHeader("X-Internal-Key", apiKey).build();
    }

    public long countByCategory(Long categoryId) {
        try {
            Long count = rest.get().uri("/internal/products/count?categoryId={id}", categoryId)
                    .retrieve().body(Long.class);
            return count == null ? 0 : count;
        } catch (RestClientException e) {
            // Không chắc còn sản phẩm hay không thì không cho xoá
            throw new ServiceUnavailableException("Product service is unavailable, cannot verify category usage");
        }
    }
}
