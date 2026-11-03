package com.fpt.authservice.service;

import com.fpt.authservice.dto.LoginRequest;
import com.fpt.authservice.dto.LoginResponse;
import com.fpt.authservice.dto.RegisterRequest;
import com.fpt.authservice.dto.UserResponse;
import com.fpt.authservice.entity.Role;
import com.fpt.authservice.entity.User;
import com.fpt.authservice.repository.RoleRepository;
import com.fpt.authservice.repository.UserRepository;
import com.fpt.authservice.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;

@Service
public class AuthService {

    private static final String INVALID_CREDENTIALS = "Invalid username/email or password";
    /** BCrypt chỉ xử lý tối đa 72 byte của mật khẩu. */
    private static final int BCRYPT_MAX_BYTES = 72;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /** Hash giả dùng khi không tìm thấy user, để thời gian phản hồi gần như bằng khi sai mật khẩu. */
    private final String dummyHash;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.dummyHash = passwordEncoder.encode("dummy-password-used-only-for-timing");
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (!fitsBcrypt(request.password())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password must be at most " + BCRYPT_MAX_BYTES + " bytes");
        }
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByUsernameIgnoreCaseOrEmailIgnoreCase(email, email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        User user = new User();
        user.setUsername(email);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFullName(blankToNull(request.fullName()));
        // Quyền luôn là USER; không nhận role từ client
        user.setRole(roleRepository.findByName("USER").orElseGet(() -> roleRepository.save(new Role("USER"))));

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String identifier = request.identifier().trim();
        Optional<User> found = userRepository.findFirstByUsernameIgnoreCaseOrEmailIgnoreCase(identifier, identifier);

        // Luôn chạy BCrypt (dùng dummyHash nếu không có user) và trả CÙNG một thông báo cho cả
        // "không tồn tại" lẫn "sai mật khẩu" để không lộ tài khoản nào có thật.
        String hash = found.map(User::getPassword).orElse(dummyHash);
        boolean passwordOk = fitsBcrypt(request.password()) && passwordEncoder.matches(request.password(), hash);
        if (found.isEmpty() || !passwordOk) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS);
        }

        User user = found.get();
        String token = jwtService.generateToken(user.getId(), user.getRole().getName());
        return LoginResponse.bearer(token, jwtService.getExpirationSeconds(), UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public UserResponse me(Long userId) {
        return userRepository.findById(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account no longer exists"));
    }

    private static boolean fitsBcrypt(String password) {
        return password != null && password.getBytes(StandardCharsets.UTF_8).length <= BCRYPT_MAX_BYTES;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
