package com.fpt.authservice.api;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.authservice.dto.LoginRequest;
import com.fpt.authservice.dto.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class AuthControllerTest extends AbstractApiTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Đăng ký thành công trả về 201 Created")
    void registerSuccess() throws Exception {
        RegisterRequest req = new RegisterRequest("test_register@test.com", "password123", "Test User");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test_register@test.com"))
                .andExpect(jsonPath("$.fullName").value("Test User"));
    }

    @Test
    @DisplayName("Đăng ký lỗi khi email không hợp lệ")
    void registerFailInvalidEmail() throws Exception {
        RegisterRequest req = new RegisterRequest("invalid-email", "password123", "Test User");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Đăng nhập thành công trả về token")
    void loginSuccess() throws Exception {
        // Đăng ký trước
        RegisterRequest req = new RegisterRequest("login@test.com", "password123", "Login User");
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        // Đăng nhập
        LoginRequest loginReq = new LoginRequest("login@test.com", "password123");
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("login@test.com"));
    }

    @Test
    @DisplayName("Đăng nhập sai mật khẩu trả về 401")
    void loginFailWrongPassword() throws Exception {
        // Đăng ký
        RegisterRequest req = new RegisterRequest("login_wrong@test.com", "password123", "Login User");
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        // Đăng nhập sai mk
        LoginRequest loginReq = new LoginRequest("login_wrong@test.com", "wrongpassword");
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Gọi /me với token hợp lệ trả về thông tin user")
    void getMeSuccess() throws Exception {
        // Đăng ký
        RegisterRequest req = new RegisterRequest("me@test.com", "password123", "Me User");
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        // Đăng nhập lấy token
        LoginRequest loginReq = new LoginRequest("me@test.com", "password123");
        String responseBody = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Lấy token từ JSON
        String token = objectMapper.readTree(responseBody).get("token").asText();

        // Gọi /me
        mockMvc.perform(get("/api/v1/auth/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("me@test.com"));
    }

    @Test
    @DisplayName("Gọi /me không có token trả về 401")
    void getMeWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Đăng ký lỗi khi email đã tồn tại (Conflict 409)")
    void registerFailDuplicateEmail() throws Exception {
        RegisterRequest req = new RegisterRequest("dup@test.com", "password123", "Dup User");
        // Đăng ký lần 1
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        // Đăng ký lần 2 với cùng email
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Đăng nhập thất bại do user không tồn tại (Unauthorized 401)")
    void loginFailUserNotFound() throws Exception {
        LoginRequest loginReq = new LoginRequest("not_found@test.com", "password123");
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Gọi /me với token rác trả về 401")
    void getMeWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }
}
