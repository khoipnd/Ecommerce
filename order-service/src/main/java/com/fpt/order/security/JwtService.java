package com.fpt.order.security;

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

/** Chỉ kiểm tra token (HS256) do auth cấp, dùng chung khóa bí mật. */
@Component
public class JwtService {

    public record Claims(Long accountId, String role) {}

    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private static final String HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private static final Pattern SUB = Pattern.compile("\"sub\":\"(\\d+)\"");
    private static final Pattern ROLE = Pattern.compile("\"role\":\"([A-Z_]+)\"");
    private static final Pattern ISS = Pattern.compile("\"iss\":\"([^\"\\\\]*)\"");
    private static final Pattern EXP = Pattern.compile("\"exp\":(\\d+)");

    private final byte[] secret;
    private final String issuer;
    private final Clock clock;

    @Autowired
    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.issuer:auth-service}") String issuer) {
        this(secret, issuer, Clock.systemUTC());
    }

    JwtService(String secret, String issuer, Clock clock) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("app.jwt.secret must be at least 32 bytes");
        }
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalStateException("app.jwt.issuer must not be blank");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.issuer = issuer;
        this.clock = clock;
    }

    public Optional<Claims> parse(String token) {
        if (token == null) return Optional.empty();
        String[] parts = token.split("\\.");
        if (parts.length != 3) return Optional.empty();
        try {
            byte[] expected = hmac(parts[0] + "." + parts[1]);
            if (!MessageDigest.isEqual(expected, DECODER.decode(parts[2]))) return Optional.empty();

            String header = new String(DECODER.decode(parts[0]), StandardCharsets.UTF_8);
            if (!HEADER_JSON.equals(header)) return Optional.empty();

            String payload = new String(DECODER.decode(parts[1]), StandardCharsets.UTF_8);
            Matcher sub = SUB.matcher(payload);
            Matcher role = ROLE.matcher(payload);
            Matcher iss = ISS.matcher(payload);
            Matcher exp = EXP.matcher(payload);
            if (!sub.find() || !role.find() || !iss.find() || !exp.find()) return Optional.empty();
            if (!issuer.equals(iss.group(1))) return Optional.empty();
            if (clock.instant().getEpochSecond() >= Long.parseLong(exp.group(1))) return Optional.empty();
            return Optional.of(new Claims(Long.parseLong(sub.group(1)), role.group(1)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private byte[] hmac(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Cannot verify token", e);
        }
    }
}
