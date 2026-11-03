package com.fpt.order.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = "12345678901234567890123456789012";
    private static final Instant NOW = Instant.ofEpochSecond(1_700_000_000L);
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        // Cấu hình một secret key hợp lệ (ít nhất 32 ký tự)
        jwtService = new JwtService(SECRET, "auth-service", Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    @DisplayName("Khởi tạo JwtService thất bại nếu secret < 32 ký tự")
    void initFailsWhenSecretTooShort() {
        assertThrows(IllegalStateException.class,
                () -> new JwtService("short", "auth-service", Clock.systemUTC()));
    }

    @Test
    @DisplayName("Parse JWT trả về empty nếu token rỗng hoặc sai format")
    void parseInvalidToken() {
        assertTrue(jwtService.parse(null).isEmpty());
        assertTrue(jwtService.parse("invalid.token").isEmpty());
        assertTrue(jwtService.parse("a.b.c").isEmpty());
    }

    @Test
    @DisplayName("Parse được JWT HS256 đúng format do auth-service tạo")
    void parseAuthServiceToken() throws Exception {
        String token = token("{\"alg\":\"HS256\",\"typ\":\"JWT\"}",
                "{\"sub\":\"1\",\"role\":\"ADMIN\",\"iss\":\"auth-service\",\"iat\":1700000000,\"exp\":1700000060}");

        JwtService.Claims claims = jwtService.parse(token).orElseThrow();

        assertEquals(1L, claims.accountId());
        assertEquals("ADMIN", claims.role());
    }

    @Test
    @DisplayName("Từ chối token sai issuer, sai algorithm hoặc đã hết hạn")
    void rejectInvalidContract() throws Exception {
        String wrongIssuer = token("{\"alg\":\"HS256\",\"typ\":\"JWT\"}",
                "{\"sub\":\"1\",\"role\":\"ADMIN\",\"iss\":\"other-service\",\"exp\":1700000060}");
        String wrongAlgorithm = token("{\"alg\":\"HS512\",\"typ\":\"JWT\"}",
                "{\"sub\":\"1\",\"role\":\"ADMIN\",\"iss\":\"auth-service\",\"exp\":1700000060}");
        String expired = token("{\"alg\":\"HS256\",\"typ\":\"JWT\"}",
                "{\"sub\":\"1\",\"role\":\"ADMIN\",\"iss\":\"auth-service\",\"exp\":1700000000}");

        assertTrue(jwtService.parse(wrongIssuer).isEmpty());
        assertTrue(jwtService.parse(wrongAlgorithm).isEmpty());
        assertTrue(jwtService.parse(expired).isEmpty());
    }

    private static String token(String header, String payload) throws Exception {
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        String unsigned = encoder.encodeToString(header.getBytes(StandardCharsets.UTF_8)) + "."
                + encoder.encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return unsigned + "." + encoder.encodeToString(mac.doFinal(unsigned.getBytes(StandardCharsets.UTF_8)));
    }
}
