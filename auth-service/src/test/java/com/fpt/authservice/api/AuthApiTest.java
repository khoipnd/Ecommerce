package com.fpt.authservice.api;

import com.fpt.authservice.entity.User;
import com.fpt.authservice.repository.UserRepository;
import com.fpt.authservice.security.JwtService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Integration test: gọi API thật qua MockMvc với JWT thật, database H2. */
class AuthApiTest extends AbstractApiTest {

    private static final String PASSWORD = "test123456";

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    // ------------------------------------------------------------ hàm hỗ trợ

    private String uniqueEmail() {
        return "user-" + System.nanoTime() + "@example.com";
    }

    private String registerBody(String email, String password) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    private void register(String email) throws Exception {
        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody(email, PASSWORD)))
                .andExpect(status().isCreated());
    }

    private String loginToken(String identifier, String password) throws Exception {
        String json = mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"" + identifier + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(json, "$.token");
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    // ------------------------------------------------------------ đăng ký

    @Test
    @DisplayName("Đăng ký thành công: 201, role USER dù client gửi role=ADMIN, không lộ mật khẩu")
    void registerCreatesUserAndIgnoresRoleFromClient() throws Exception {
        String email = uniqueEmail();

        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD
                                + "\",\"fullName\":\"Nguyen Van A\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.username").value(email))
                .andExpect(jsonPath("$.fullName").value("Nguyen Van A"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("Mật khẩu lưu trong database là hash BCrypt, không phải mật khẩu thô")
    void passwordIsStoredAsBcryptHash() throws Exception {
        String email = uniqueEmail();
        register(email);

        User saved = userRepository.findFirstByUsernameIgnoreCaseOrEmailIgnoreCase(email, email).orElseThrow();

        assertTrue(saved.getPassword().startsWith("$2"), saved.getPassword());
        assertEquals(60, saved.getPassword().length());
        assertTrue(passwordEncoder.matches(PASSWORD, saved.getPassword()));
    }

    @Test
    @DisplayName("Email được chuẩn hóa về chữ thường")
    void registerNormalizesEmail() throws Exception {
        String email = uniqueEmail();

        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody(email.toUpperCase(), PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    @DisplayName("Đăng ký trùng email (không phân biệt hoa thường) -> 409")
    void registerDuplicateEmailIsConflict() throws Exception {
        String email = uniqueEmail();
        register(email);

        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody(email.toUpperCase(), PASSWORD)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Email already registered"));
    }

    @Test
    @DisplayName("Đăng ký sai dữ liệu (email sai, mật khẩu ngắn, để trống) -> 400 theo từng trường")
    void registerValidation() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("not-an-email", PASSWORD)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists());

        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody(uniqueEmail(), "123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").exists());

        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.password").exists());
    }

    @Test
    @DisplayName("Mật khẩu quá 72 byte (BCrypt không hỗ trợ) bị từ chối dù chưa quá 72 ký tự")
    void registerRejectsPasswordOver72Bytes() throws Exception {
        String longMultibyte = "é".repeat(40); // 40 ký tự nhưng 80 byte

        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody(uniqueEmail(), longMultibyte)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("JSON sai cú pháp -> 400 {error}")
    void registerMalformedJson() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content("{bad"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    // ------------------------------------------------------------ đăng nhập

    @Test
    @DisplayName("Đăng nhập bằng email trả token, role USER; token có đúng 3 phần")
    void loginWithEmail() throws Exception {
        String email = uniqueEmail();
        register(email);

        String json = mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresInSeconds").value(3600))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.email").value(email))
                .andReturn().getResponse().getContentAsString();

        String token = JsonPath.read(json, "$.token");
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    @DisplayName("Admin mẫu đăng nhập được bằng username hoặc email; token mang role ADMIN")
    void adminLoginByUsernameOrEmail() throws Exception {
        String byUsername = loginToken("admin", "123456");
        String byEmail = loginToken("ADMIN@gmail.com", "123456");

        String payload = new String(Base64.getUrlDecoder().decode(byUsername.split("\\.")[1]), StandardCharsets.UTF_8);
        assertTrue(payload.contains("\"role\":\"ADMIN\""), payload);
        assertEquals(3, byEmail.split("\\.").length);
    }

    @Test
    @DisplayName("Sai mật khẩu và tài khoản không tồn tại cùng trả 401 với cùng thông báo")
    void wrongPasswordAndUnknownUserLookTheSame() throws Exception {
        String email = uniqueEmail();
        register(email);

        String wrongPassword = mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"" + email + "\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        String unknownUser = mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"nobody-" + System.nanoTime() + "@example.com\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        assertEquals(wrongPassword, unknownUser);
        assertTrue(wrongPassword.contains("Invalid username/email or password"), wrongPassword);
    }

    @Test
    @DisplayName("Đăng nhập thiếu trường -> 400; mật khẩu quá dài -> 401")
    void loginValidation() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.identifier").exists())
                .andExpect(jsonPath("$.password").exists());

        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"admin\",\"password\":\"" + "a".repeat(100) + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Endpoint công khai vẫn dùng được khi client gửi kèm token rác")
    void publicEndpointsIgnoreInvalidToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login").header("Authorization", "Bearer garbage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"admin\",\"password\":\"123456\"}"))
                .andExpect(status().isOk());
    }

    // ------------------------------------------------------------ /me và xác thực

    @Test
    @DisplayName("/me với token hợp lệ trả đúng tài khoản")
    void meReturnsCurrentUser() throws Exception {
        String email = uniqueEmail();
        register(email);
        String token = loginToken(email, PASSWORD);

        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("/me không token, token rác, sai scheme -> 401")
    void meRequiresValidToken() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer not.a.jwt"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", "Basic abc"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Token bị sửa role USER thành ADMIN -> 401")
    void tamperedTokenRejected() throws Exception {
        String email = uniqueEmail();
        register(email);
        String[] parts = loginToken(email, PASSWORD).split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8)
                .replace("\"role\":\"USER\"", "\"role\":\"ADMIN\"");
        String forged = parts[0] + "." + Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8)) + "." + parts[2];

        mockMvc.perform(get("/api/v1/users").header("Authorization", bearer(forged)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Token đã hết hạn -> 401")
    void expiredTokenRejected() throws Exception {
        JwtService twoHoursAgo = new JwtService(jwtSecret, 60, "auth-service",
                Clock.offset(Clock.systemUTC(), Duration.ofHours(-2)));
        String expired = twoHoursAgo.generateToken(1L, "ADMIN");

        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", bearer(expired)))
                .andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------------------ phân quyền ADMIN / USER

    @Test
    @DisplayName("ADMIN xem được danh sách user; danh sách không có mật khẩu")
    void adminCanListUsers() throws Exception {
        String email = uniqueEmail();
        register(email);
        String admin = loginToken("admin", "123456");

        mockMvc.perform(get("/api/v1/users").header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].password").doesNotExist())
                .andExpect(jsonPath("$[?(@.email == '" + email + "')].role").value("USER"));
    }

    @Test
    @DisplayName("USER thường gọi /users -> 403; chưa đăng nhập -> 401")
    void userIsForbiddenAnonymousIsUnauthorized() throws Exception {
        String email = uniqueEmail();
        register(email);
        String userToken = loginToken(email, PASSWORD);

        mockMvc.perform(get("/api/v1/users").header("Authorization", bearer(userToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());

        mockMvc.perform(get("/api/v1/users")).andExpect(status().isUnauthorized());
    }
}
