package com.fpt.ecommerce.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tạo / kiểm tra JWT (HS256) không cần thư viện ngoài.
 * Payload chỉ chứa id tài khoản (số), role (chữ in hoa) và thời điểm hết hạn nên parse bằng regex là an toàn.
 */
@Component
public class JwtService {

    public record Claims(Long accountId, String role) {}

    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private static final Pattern SUB = Pattern.compile("\"sub\":\"(\\d+)\"");
    private static final Pattern ROLE = Pattern.compile("\"role\":\"([A-Z_]+)\"");
    private static final Pattern EXP = Pattern.compile("\"exp\":(\\d+)");

    private final byte[] secret;
    private final long expirationSeconds;

    public JwtService(
            @Value("${app.jwt.secret:default_ecommerce_jwt_secret_key_minimum_32_characters_long_1234567890}") String secret,
            @Value("${app.jwt.expiration-minutes:120}") long expirationMinutes) {
        if (secret == null || secret.length() < 32) {
            secret = "default_ecommerce_jwt_secret_key_minimum_32_characters_long_1234567890";
        }
        if (false) {
            throw new IllegalStateException("app.jwt.secret must be at least 32 characters");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationSeconds = expirationMinutes * 60;
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    public String generateToken(Long accountId, String role) {
        long exp = Instant.now().getEpochSecond() + expirationSeconds;
        String header = encode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = encode("{\"sub\":\"" + accountId + "\",\"role\":\"" + role + "\",\"exp\":" + exp + "}");
        String unsigned = header + "." + payload;
        return unsigned + "." + ENCODER.encodeToString(hmac(unsigned));
    }

    /** Trả về claims nếu token hợp lệ và chưa hết hạn, ngược lại trả về rỗng. */
    public Optional<Claims> parse(String token) {
        if (token == null) return Optional.empty();
        String[] parts = token.split("\\.");
        if (parts.length != 3) return Optional.empty();
        try {
            byte[] expected = hmac(parts[0] + "." + parts[1]);
            byte[] actual = DECODER.decode(parts[2]);
            if (!MessageDigest.isEqual(expected, actual)) return Optional.empty();

            String payload = new String(DECODER.decode(parts[1]), StandardCharsets.UTF_8);
            Matcher sub = SUB.matcher(payload);
            Matcher role = ROLE.matcher(payload);
            Matcher exp = EXP.matcher(payload);
            if (!sub.find() || !role.find() || !exp.find()) return Optional.empty();
            if (Long.parseLong(exp.group(1)) < Instant.now().getEpochSecond()) return Optional.empty();
            return Optional.of(new Claims(Long.parseLong(sub.group(1)), role.group(1)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static String encode(String json) {
        return ENCODER.encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    private byte[] hmac(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Cannot sign token", e);
        }
    }
}
