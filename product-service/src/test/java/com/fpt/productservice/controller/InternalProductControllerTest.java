package com.fpt.productservice.controller;

import com.fpt.productservice.entity.Product;
import com.fpt.productservice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class InternalProductControllerTest {
    @Autowired WebApplicationContext context;
    @Autowired ProductRepository products;

    @Test
    void requiresKeyAndChangesStockAtomically() throws Exception {
        Product product = new Product();
        product.setName("Stock test");
        product.setDescription("test");
        product.setPrice(BigDecimal.TEN);
        product.setStockQuantity(3);
        product.setCategoryId(1L);
        long id = products.save(product).getId();
        MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
        String url = "/internal/products/" + id + "/stock/decrease";
        String body = "{\"quantity\":2}";

        mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mvc.perform(post(url).header("X-Internal-Key", "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        assertEquals(1, products.findById(id).orElseThrow().getStockQuantity());
        mvc.perform(post(url).header("X-Internal-Key", "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
        assertEquals(1, products.findById(id).orElseThrow().getStockQuantity());
        mvc.perform(post("/internal/products/" + id + "/stock/increase")
                        .header("X-Internal-Key", "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        assertEquals(3, products.findById(id).orElseThrow().getStockQuantity());
        products.deleteById(id);
    }

    @Test
    void countsProductsByCategoryWithKeyOnly() throws Exception {
        Product product = new Product();
        product.setName("Count test");
        product.setPrice(BigDecimal.TEN);
        product.setStockQuantity(1);
        product.setCategoryId(777L);
        long id = products.save(product).getId();
        MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();

        mvc.perform(get("/internal/products/count").param("categoryId", "777"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/internal/products/count").param("categoryId", "777")
                        .header("X-Internal-Key", "test-internal-key"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
        mvc.perform(get("/internal/products/count").param("categoryId", "778")
                        .header("X-Internal-Key", "test-internal-key"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
        products.deleteById(id);
    }
}
