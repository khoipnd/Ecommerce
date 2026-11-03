package com.fpt.productservice.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AdminWriteFilterTest {
    @Test
    void rejectsMissingToken() throws Exception {
        AdminWriteFilter filter = new AdminWriteFilter(RestClient.builder(), "http://auth-service");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/products");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> fail("Write was not blocked"));
        assertEquals(401, response.getStatus());
    }

    @Test
    void requiresAdminRole() throws Exception {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://auth-service/api/v1/auth/me"))
                .andRespond(withSuccess("{\"role\":\"USER\"}", MediaType.APPLICATION_JSON));
        AdminWriteFilter filter = new AdminWriteFilter(builder, "http://auth-service");
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/api/v1/products/1");
        request.addHeader("Authorization", "Bearer test-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> fail("User write was not blocked"));
        assertEquals(403, response.getStatus());
        server.verify();
    }

    @Test
    void allowsAdminWriteAndPublicRead() throws Exception {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://auth-service/api/v1/auth/me"))
                .andRespond(withSuccess("{\"role\":\"ADMIN\"}", MediaType.APPLICATION_JSON));
        AdminWriteFilter filter = new AdminWriteFilter(builder, "http://auth-service");
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/api/v1/products/1");
        request.addHeader("Authorization", "Bearer test-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean reached = new AtomicBoolean();
        filter.doFilter(request, response, (req, res) -> reached.set(true));
        assertTrue(reached.get());
        server.verify();

        reached.set(false);
        MockHttpServletRequest read = new MockHttpServletRequest("GET", "/api/v1/products");
        filter.doFilter(read, new MockHttpServletResponse(), (req, res) -> reached.set(true));
        assertTrue(reached.get());
    }
}
