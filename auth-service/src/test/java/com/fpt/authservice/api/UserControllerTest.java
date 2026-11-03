package com.fpt.authservice.api;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.authservice.dto.LoginRequest;
import com.fpt.authservice.dto.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class UserControllerTest extends AbstractApiTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Lấy danh sách users thành công (cần quyền ADMIN)")
    void getAllUsersByAdmin() throws Exception {
        // DataInitializer tự động tạo tài khoản admin/123456
        LoginRequest loginReq = new LoginRequest("admin", "123456");
        String responseBody = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(responseBody).get("token").asText();

        // Gọi API lấy users
        mockMvc.perform(get("/api/v1/users")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].email").exists())
                .andExpect(jsonPath("$[0].role").exists())
                .andExpect(jsonPath("$[0].password").doesNotExist()); // Đảm bảo không lộ password
    }

    @Test
    @DisplayName("Lấy danh sách users thất bại khi là USER (throw 403)")
    void getAllUsersByUserRole() throws Exception {
        // Tạo user thường
        RegisterRequest req = new RegisterRequest("normal@user.com", "password123", "Normal User");
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        // Login user thường
        LoginRequest loginReq = new LoginRequest("normal@user.com", "password123");
        String responseBody = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(responseBody).get("token").asText();

        // Gọi API lấy users => 403 Forbidden do ko có quyền ADMIN
        mockMvc.perform(get("/api/v1/users")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Lấy danh sách users thất bại khi không có token (throw 401)")
    void getAllUsersWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Lấy danh sách users thất bại với token rác (throw 401)")
    void getAllUsersWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                .header("Authorization", "Bearer invalid-jwt-token-xyz"))
                .andExpect(status().isUnauthorized());
    }
}
