package com.fpt.authservice.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test thuần (không Spring, không DB) cho việc SINH và KIỂM TRA JWT.
 * Dùng Clock cố định nên kiểm tra hết hạn chính xác đến từng giây, không cần chờ thật.
 */
class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-key-at-least-32-characters!!";
    private static final String ISSUER = "auth-service";
    private static final Instant T0 = Instant.parse("2026-10-01T00:00:00Z");
    private static final Clock CLOCK = Clock.fixed(T0, ZoneOffset.UTC);
    private static final String HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";

    private final JwtService jwt = new JwtService(SECRET, 60, ISSUER, CLOCK);

    private static String decode(String part) {
        return new String(Base64.getUrlDecoder().decode(part), StandardCharsets.UTF_8);
    }

    private static String encode(String text) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    // ------------------------------------------------------------------ SINH TOKEN

    @Test
    @DisplayName("Token có 3 phần header.payload.signature, header là HS256/JWT")
    void generatedTokenHasThreePartsAndHs256Header() {
        String[] parts = jwt.generateToken(42L, "USER").split("\\.");

        assertEquals(3, parts.length);
        assertEquals(HEADER_JSON, decode(parts[0]));
    }

    @Test
    @DisplayName("Payload chứa sub, role, iss, iat, exp đúng giá trị")
    void payloadContainsExpectedClaims() {
        String payload = decode(jwt.generateToken(42L, "ADMIN").split("\\.")[1]);

        assertTrue(payload.contains("\"sub\":\"42\""), payload);
        assertTrue(payload.contains("\"role\":\"ADMIN\""), payload);
        assertTrue(payload.contains("\"iss\":\"auth-service\""), payload);
        assertTrue(payload.contains("\"iat\":" + T0.getEpochSecond()), payload);
        assertTrue(payload.contains("\"exp\":" + (T0.getEpochSecond() + 3600)), payload);
    }

    @Test
    @DisplayName("Token dùng base64url, không có ký tự +, / hoặc =")
    void tokenIsUrlSafe() {
        String token = jwt.generateToken(7L, "USER");

        assertFalse(token.contains("+"));
        assertFalse(token.contains("/"));
        assertFalse(token.contains("="));
    }

    @Test
    @DisplayName("Cùng đầu vào, cùng thời điểm thì cùng token (HMAC xác định)")
    void signingIsDeterministic() {
        assertEquals(jwt.generateToken(1L, "USER"), jwt.generateToken(1L, "USER"));
    }

    @Test
    @DisplayName("Các id/role khác nhau cho token khác nhau")
    void differentInputsGiveDifferentTokens() {
        assertFalse(jwt.generateToken(1L, "USER").equals(jwt.generateToken(2L, "USER")));
        assertFalse(jwt.generateToken(1L, "USER").equals(jwt.generateToken(1L, "ADMIN")));
    }

    @Test
    @DisplayName("Từ chối userId null và role sai định dạng (chống chèn JSON vào payload)")
    void generateRejectsInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> jwt.generateToken(null, "USER"));
        assertThrows(IllegalArgumentException.class, () -> jwt.generateToken(1L, null));
        assertThrows(IllegalArgumentException.class, () -> jwt.generateToken(1L, "admin"));
        assertThrows(IllegalArgumentException.class, () -> jwt.generateToken(1L, "ADMIN\",\"x\":\"y"));
    }

    // ------------------------------------------------------------------ KIỂM TRA TOKEN HỢP LỆ

    @Test
    @DisplayName("Token hợp lệ được parse ra đúng id và role")
    void parseReturnsClaims() {
        Optional<JwtService.Claims> claims = jwt.parse(jwt.generateToken(42L, "ADMIN"));

        assertTrue(claims.isPresent());
        assertEquals(42L, claims.get().userId().longValue());
        assertEquals("ADMIN", claims.get().role());
    }

    @Test
    @DisplayName("getExpirationSeconds = số phút x 60")
    void expirationSeconds() {
        assertEquals(3600L, jwt.getExpirationSeconds());
    }

    // ------------------------------------------------------------------ HẾT HẠN

    @Test
    @DisplayName("Còn hạn đến giây cuối, hết hạn đúng tại thời điểm exp")
    void expiryBoundary() {
        String token = jwt.generateToken(1L, "USER");

        JwtService justBefore = new JwtService(SECRET, 60, ISSUER, Clock.offset(CLOCK, Duration.ofSeconds(3599)));
        JwtService atExpiry = new JwtService(SECRET, 60, ISSUER, Clock.offset(CLOCK, Duration.ofSeconds(3600)));

        assertTrue(justBefore.parse(token).isPresent());
        assertTrue(atExpiry.parse(token).isEmpty());
    }

    @Test
    @DisplayName("Token đã hết hạn nhiều giờ bị từ chối")
    void expiredTokenRejected() {
        String token = jwt.generateToken(1L, "USER");
        JwtService later = new JwtService(SECRET, 60, ISSUER, Clock.offset(CLOCK, Duration.ofHours(5)));

        assertTrue(later.parse(token).isEmpty());
    }

    // ------------------------------------------------------------------ GIẢ MẠO

    @Test
    @DisplayName("Sửa payload (USER thành ADMIN) nhưng giữ chữ ký cũ thì bị từ chối")
    void tamperedPayloadRejected() {
        String[] parts = jwt.generateToken(5L, "USER").split("\\.");
        String forgedPayload = encode(decode(parts[1]).replace("\"role\":\"USER\"", "\"role\":\"ADMIN\""));

        assertTrue(jwt.parse(parts[0] + "." + forgedPayload + "." + parts[2]).isEmpty());
    }

    @Test
    @DisplayName("Sửa chữ ký thì bị từ chối")
    void tamperedSignatureRejected() {
        String[] parts = jwt.generateToken(5L, "USER").split("\\.");
        char first = parts[2].charAt(0);
        String badSignature = (first == 'A' ? 'B' : 'A') + parts[2].substring(1);

        assertTrue(jwt.parse(parts[0] + "." + parts[1] + "." + badSignature).isEmpty());
    }

    @Test
    @DisplayName("Token ký bằng secret khác bị từ chối")
    void wrongSecretRejected() {
        JwtService attacker = new JwtService("another-secret-key-also-32-chars-long!!", 60, ISSUER, CLOCK);

        assertTrue(jwt.parse(attacker.generateToken(1L, "ADMIN")).isEmpty());
    }

    @Test
    @DisplayName("Token của issuer khác bị từ chối")
    void differentIssuerRejected() {
        JwtService other = new JwtService(SECRET, 60, "other-service", CLOCK);

        assertTrue(jwt.parse(other.generateToken(1L, "USER")).isEmpty());
    }

    @Test
    @DisplayName("Tấn công alg=none bị từ chối")
    void algNoneRejected() {
        String[] parts = jwt.generateToken(1L, "ADMIN").split("\\.");
        String noneHeader = encode("{\"alg\":\"none\",\"typ\":\"JWT\"}");

        // không có chữ ký
        assertTrue(jwt.parse(noneHeader + "." + parts[1] + ".").isEmpty());
        // thay header nhưng giữ chữ ký cũ
        assertTrue(jwt.parse(noneHeader + "." + parts[1] + "." + parts[2]).isEmpty());
    }

    @Test
    @DisplayName("Chuỗi không phải JWT bị từ chối, không ném exception")
    void malformedTokensRejected() {
        String[] bad = {null, "", "   ", "abc", "a.b", "a.b.c.d", "!!!.@@@.###", "....", "Bearer x.y.z"};

        for (String token : bad) {
            assertTrue(jwt.parse(token).isEmpty(), "Phải từ chối: " + token);
        }
    }

    // ------------------------------------------------------------------ CẤU HÌNH

    @Test
    @DisplayName("Secret ngắn hơn 32 byte, thời hạn <= 0 hoặc issuer sai bị từ chối ngay khi khởi tạo")
    void constructorRejectsWeakConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> new JwtService("short", 60, ISSUER, CLOCK));
        assertThrows(IllegalArgumentException.class, () -> new JwtService(null, 60, ISSUER, CLOCK));
        assertThrows(IllegalArgumentException.class, () -> new JwtService(SECRET, 0, ISSUER, CLOCK));
        assertThrows(IllegalArgumentException.class, () -> new JwtService(SECRET, -5, ISSUER, CLOCK));
        assertThrows(IllegalArgumentException.class, () -> new JwtService(SECRET, 60, " ", CLOCK));
        assertThrows(IllegalArgumentException.class, () -> new JwtService(SECRET, 60, "bad\"issuer", CLOCK));
    }
}
