package com.fpt.order.client;

import com.fpt.order.dto.ProductInfo;
import com.fpt.order.exception.ResourceNotFoundException;
import com.fpt.order.exception.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/** Gọi sang product-service (lấy thông tin sản phẩm, trừ / hoàn kho). */
@Component
public class ProductClient {

    private final RestClient rest;

    public ProductClient(RestClient.Builder builder,
                         @Value("${product-service.url}") String baseUrl,
                         @Value("${internal.api-key}") String apiKey) {
        this.rest = builder.baseUrl(baseUrl).defaultHeader("X-Internal-Key", apiKey).build();
    }

    public ProductInfo getProduct(Long id) {
        try {
            return rest.get().uri("/api/v1/products/{id}", id).retrieve().body(ProductInfo.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Product not found with id = " + id);
        } catch (RestClientException e) {
            throw new ServiceUnavailableException("Product service is unavailable");
        }
    }

    /** true nếu trừ kho thành công, false nếu không đủ hàng. */
    public boolean decreaseStock(Long id, int quantity) {
        try {
            rest.post().uri("/internal/products/{id}/stock/decrease", id)
                    .body(Map.of("quantity", quantity)).retrieve().toBodilessEntity();
            return true;
        } catch (HttpClientErrorException.Conflict e) {
            return false;
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Product not found with id = " + id);
        } catch (RestClientException e) {
            throw new ServiceUnavailableException("Product service is unavailable");
        }
    }

    public void increaseStock(Long id, int quantity) {
        try {
            rest.post().uri("/internal/products/{id}/stock/increase", id)
                    .body(Map.of("quantity", quantity)).retrieve().toBodilessEntity();
        } catch (RestClientException e) {
            throw new ServiceUnavailableException("Product service is unavailable");
        }
    }
}
