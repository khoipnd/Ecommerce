package com.fpt.authservice.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tạo và kiểm tra JWT (JSON Web Token) ký bằng HS256 (HMAC-SHA256), không cần thư viện ngoài.
 *
 * Cấu trúc token: base64url(header) . base64url(payload) . base64url(signature)
 * Payload: sub (id tài khoản), role, iss (nơi phát hành), iat (thời điểm phát), exp (thời điểm hết hạn, giây epoch).
 * Các tên claim sub/role/exp giống JwtService của backend hiện tại.
 *
 * An toàn khi parse payload bằng regex vì chỉ parse SAU khi chữ ký HMAC đã khớp, tức payload do chính service này ký ra
 * (id là số, role là chữ in hoa, issuer đã kiểm tra không chứa dấu nháy).
 */
@Component
public class JwtService {

    /** Dữ liệu lấy ra từ token hợp lệ. */
    public record Claims(Long userId, String role) {
    }

    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    /** Header cố định. Token có header khác (ví dụ alg=none) bị từ chối ngay. */
    private static final String HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";

    private static final Pattern ROLE_NAME = Pattern.compile("[A-Z_]+");
    private static final Pattern SUB = Pattern.compile("\"sub\":\"(\\d+)\"");
    private static final Pattern ROLE = Pattern.compile("\"role\":\"([A-Z_]+)\"");
    private static final Pattern ISS = Pattern.compile("\"iss\":\"([^\"\\\\]*)\"");
    private static final Pattern EXP = Pattern.compile("\"exp\":(\\d+)");

    private final byte[] secret;
    private final long expirationSeconds;
    private final String issuer;
    private final Clock clock;

    /** Constructor Spring dùng (đọc cấu hình từ application.properties). */
    @Autowired
    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-minutes:120}") long expirationMinutes,
            @Value("${app.jwt.issuer:auth-service}") String issuer) {
        this(secret, expirationMinutes, issuer, Clock.systemUTC());
    }

    /** Constructor cho test: truyền Clock giả để kiểm tra hết hạn mà không cần chờ thật. */
    public JwtService(String secret, long expirationMinutes, String issuer, Clock clock) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("app.jwt.secret must be at least 32 bytes");
        }
        if (expirationMinutes <= 0) {
            throw new IllegalArgumentException("app.jwt.expiration-minutes must be greater than 0");
        }
        if (issuer == null || issuer.isBlank() || issuer.indexOf('"') >= 0 || issuer.indexOf('\\') >= 0) {
            throw new IllegalArgumentException("app.jwt.issuer must not be blank and must not contain quotes or backslashes");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationSeconds = expirationMinutes * 60;
        this.issuer = issuer;
        this.clock = clock;
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    public String generateToken(Long userId, String role) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        if (role == null || !ROLE_NAME.matcher(role).matches()) {
            throw new IllegalArgumentException("role must contain only A-Z and underscore");
        }
        long now = clock.instant().getEpochSecond();
        long exp = now + expirationSeconds;

        String payloadJson = "{\"sub\":\"" + userId + "\",\"role\":\"" + role + "\",\"iss\":\"" + issuer
                + "\",\"iat\":" + now + ",\"exp\":" + exp + "}";
        String unsigned = encode(HEADER_JSON) + "." + encode(payloadJson);
        return unsigned + "." + ENCODER.encodeToString(hmac(unsigned));
    }

    /** Trả về claims nếu token đúng chữ ký, đúng issuer và chưa hết hạn; ngược lại trả về rỗng (không ném lỗi). */
    public Optional<Claims> parse(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return Optional.empty();
        }
        try {
            // 1. Kiểm tra chữ ký trước (so sánh hằng thời gian để tránh timing attack)
            byte[] expected = hmac(parts[0] + "." + parts[1]);
            byte[] actual = DECODER.decode(parts[2]);
            if (!MessageDigest.isEqual(expected, actual)) {
                return Optional.empty();
            }
            // 2. Header phải đúng HS256
            if (!HEADER_JSON.equals(new String(DECODER.decode(parts[0]), StandardCharsets.UTF_8))) {
                return Optional.empty();
            }
            // 3. Đọc payload (đã được ký nên đáng tin)
            String payload = new String(DECODER.decode(parts[1]), StandardCharsets.UTF_8);
            Matcher sub = SUB.matcher(payload);
            Matcher role = ROLE.matcher(payload);
            Matcher iss = ISS.matcher(payload);
            Matcher exp = EXP.matcher(payload);
            if (!sub.find() || !role.find() || !iss.find() || !exp.find()) {
                return Optional.empty();
            }
            if (!issuer.equals(iss.group(1))) {
                return Optional.empty();
            }
            // 4. Hết hạn khi thời điểm hiện tại >= exp
            if (clock.instant().getEpochSecond() >= Long.parseLong(exp.group(1))) {
                return Optional.empty();
            }
            return Optional.of(new Claims(Long.parseLong(sub.group(1)), role.group(1)));
        } catch (IllegalArgumentException e) {
            // Base64 sai định dạng hoặc số quá lớn
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
