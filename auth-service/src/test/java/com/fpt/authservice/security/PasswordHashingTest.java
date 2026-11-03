package com.fpt.authservice.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test thuần cho mã hóa mật khẩu BCrypt (không Spring context, không DB).
 * Dùng đúng lớp BCryptPasswordEncoder mà PasswordConfig tạo ra.
 */
class PasswordHashingTest {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder(10);

    @Test
    @DisplayName("Hash khác mật khẩu gốc, không chứa mật khẩu thô")
    void hashIsNotPlainText() {
        String hash = encoder.encode("MySecret123");

        assertNotEquals("MySecret123", hash);
        assertFalse(hash.contains("MySecret123"));
    }

    @Test
    @DisplayName("Định dạng BCrypt: $2a$10$ + 53 ký tự, tổng 60 ký tự")
    void hashHasBcryptFormat() {
        String hash = encoder.encode("MySecret123");

        assertEquals(60, hash.length());
        assertTrue(hash.matches("^\\$2[aby]\\$10\\$.{53}$"), hash);
    }

    @Test
    @DisplayName("Cùng mật khẩu hash hai lần ra hai chuỗi khác nhau (salt ngẫu nhiên) nhưng cùng khớp")
    void saltMakesHashesDifferent() {
        String first = encoder.encode("MySecret123");
        String second = encoder.encode("MySecret123");

        assertNotEquals(first, second);
        assertTrue(encoder.matches("MySecret123", first));
        assertTrue(encoder.matches("MySecret123", second));
    }

    @Test
    @DisplayName("matches đúng với mật khẩu đúng, sai với mật khẩu sai (có phân biệt hoa thường)")
    void matchesCorrectAndRejectsWrong() {
        String hash = encoder.encode("MySecret123");

        assertTrue(encoder.matches("MySecret123", hash));
        assertFalse(encoder.matches("mysecret123", hash));
        assertFalse(encoder.matches("MySecret124", hash));
        assertFalse(encoder.matches("", hash));
    }

    @Test
    @DisplayName("Cost nằm trong hash: hash cost 4 vẫn được encoder cost 10 kiểm tra đúng")
    void costFactorIsStoredInHash() {
        String lowCostHash = new BCryptPasswordEncoder(4).encode("MySecret123");

        assertTrue(lowCostHash.startsWith("$2a$04$"), lowCostHash);
        assertTrue(encoder.matches("MySecret123", lowCostHash));
    }

    @Test
    @DisplayName("Mật khẩu tiếng Việt có dấu hoạt động bình thường")
    void unicodePasswordWorks() {
        String hash = encoder.encode("Mật khẩu 123");

        assertTrue(encoder.matches("Mật khẩu 123", hash));
        assertFalse(encoder.matches("Mat khau 123", hash));
    }

    @Test
    @DisplayName("Chuỗi không phải hash BCrypt thì matches trả false, không ném lỗi")
    void garbageHashDoesNotMatch() {
        assertFalse(encoder.matches("MySecret123", "not-a-bcrypt-hash"));
        assertFalse(encoder.matches("MySecret123", "MySecret123"));
    }



    @Test
    @DisplayName("BCrypt mặc định trong Spring Security sẽ ném lỗi nếu mật khẩu dài quá 72 bytes")
    void encodeThrowsExceptionForPasswordOver72Bytes() {
        String longPassword = "a".repeat(73); // 73 bytes
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            encoder.encode(longPassword);
        });
    }
}
