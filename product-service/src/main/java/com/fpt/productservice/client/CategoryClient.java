package com.fpt.productservice.client;

import com.fpt.productservice.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class CategoryClient {

    private final RestClient restClient;

    public CategoryClient(
            RestClient.Builder restClientBuilder,
            @Value("${category-service.url}") String categoryServiceUrl
    ) {
        this.restClient = restClientBuilder
                .baseUrl(categoryServiceUrl)
                .build();
    }

    public CategoryResponse getCategoryById(Long id) {

        try {
            return restClient.get()
                    .uri("/api/v1/categories/{id}", id)
                    .retrieve()
                    .body(CategoryResponse.class);

        } catch (HttpClientErrorException.NotFound e) {

            throw new ResourceNotFoundException(
                    "Category not found with id = " + id
            );
        }
    }
}