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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        // Cần mock hàm encode trước vì constructor của AuthService gọi nó (dummyHash)
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-dummy");
        authService = new AuthService(userRepository, roleRepository, passwordEncoder, jwtService);
    }

    @Test
    @DisplayName("Đăng ký thành công trả về UserResponse")
    void registerSuccess() {
        RegisterRequest request = new RegisterRequest("test@test.com", "password123", "Test User");

        when(userRepository.existsByUsernameIgnoreCaseOrEmailIgnoreCase("test@test.com", "test@test.com"))
                .thenReturn(false);

        Role userRole = new Role("USER");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("test@test.com");
        savedUser.setUsername("test@test.com");
        savedUser.setFullName("Test User");
        savedUser.setRole(userRole);

        // passwordEncoder cũng sẽ được gọi khi mã hóa password thật
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("test@test.com", response.email());
        assertEquals("USER", response.role());
    }

    @Test
    @DisplayName("Đăng ký thất bại khi email đã tồn tại (throw 409 Conflict)")
    void registerFailEmailExists() {
        RegisterRequest request = new RegisterRequest("exist@test.com", "password123", "Test User");

        when(userRepository.existsByUsernameIgnoreCaseOrEmailIgnoreCase("exist@test.com", "exist@test.com"))
                .thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.register(request));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals("Email already registered", ex.getReason());
    }

    @Test
    @DisplayName("Đăng ký thất bại khi mật khẩu quá 72 bytes (throw 400 Bad Request)")
    void registerFailPasswordTooLong() {
        // Tạo mật khẩu dài hơn 72 ký tự
        String longPassword = "a".repeat(73);
        RegisterRequest request = new RegisterRequest("test@test.com", longPassword, "Test User");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.register(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Password must be at most 72 bytes"));
    }

    @Test
    @DisplayName("Đăng nhập thành công trả về token")
    void loginSuccess() {
        LoginRequest request = new LoginRequest("test@test.com", "password123");

        Role userRole = new Role("USER");
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setPassword("hashed-password");
        user.setRole(userRole);

        when(userRepository.findFirstByUsernameIgnoreCaseOrEmailIgnoreCase("test@test.com", "test@test.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed-password")).thenReturn(true);
        when(jwtService.generateToken(1L, "USER")).thenReturn("fake-jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("fake-jwt-token", response.token());
        assertEquals(1L, response.id());
        assertEquals("test@test.com", response.email());
    }

    @Test
    @DisplayName("Đăng nhập thất bại do sai mật khẩu (throw 401 Unauthorized)")
    void loginFailWrongPassword() {
        LoginRequest request = new LoginRequest("test@test.com", "wrongpassword");

        User user = new User();
        user.setPassword("hashed-password");

        when(userRepository.findFirstByUsernameIgnoreCaseOrEmailIgnoreCase("test@test.com", "test@test.com"))
                .thenReturn(Optional.of(user));
        // Giả lập sai mật khẩu
        when(passwordEncoder.matches("wrongpassword", "hashed-password")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.login(request));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertEquals("Invalid username/email or password", ex.getReason());
    }

    @Test
    @DisplayName("Đăng nhập thất bại do user không tồn tại (throw 401 Unauthorized)")
    void loginFailUserNotFound() {
        LoginRequest request = new LoginRequest("notfound@test.com", "password123");

        when(userRepository.findFirstByUsernameIgnoreCaseOrEmailIgnoreCase("notfound@test.com", "notfound@test.com"))
                .thenReturn(Optional.empty());
        
        // Khi không tìm thấy user, dummyHash sẽ được lấy ra từ constructor và đưa vào so khớp
        when(passwordEncoder.matches("password123", "hashed-dummy")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.login(request));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertEquals("Invalid username/email or password", ex.getReason());
    }

    @Test
    @DisplayName("Lấy thông tin profile /me thành công")
    void meSuccess() {
        Role userRole = new Role("USER");
        User user = new User();
        user.setId(1L);
        user.setEmail("me@test.com");
        user.setRole(userRole);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = authService.me(1L);
        assertNotNull(response);
        assertEquals("me@test.com", response.email());
    }

    @Test
    @DisplayName("Lấy thông tin profile /me thất bại do user bị xóa (throw 401)")
    void meFailUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.me(1L));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertEquals("Account no longer exists", ex.getReason());
    }

    @Test
    @DisplayName("Đăng ký thành công nhưng fullName rỗng hoặc khoảng trắng thì sẽ bị lưu là null")
    void registerWithBlankFullNameSetsToNull() {
        RegisterRequest request = new RegisterRequest("blank@test.com", "password123", "   ");

        when(userRepository.existsByUsernameIgnoreCaseOrEmailIgnoreCase("blank@test.com", "blank@test.com"))
                .thenReturn(false);

        Role userRole = new Role("USER");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            userToSave.setId(2L);
            return userToSave;
        });

        UserResponse response = authService.register(request);

        assertNotNull(response);
        assertNull(response.fullName());
    }

    @Test
    @DisplayName("Đăng ký sẽ tự tạo mới role USER nếu DB chưa có")
    void registerCreatesUserRoleIfNotFound() {
        RegisterRequest request = new RegisterRequest("newrole@test.com", "password123", "Test User");

        when(userRepository.existsByUsernameIgnoreCaseOrEmailIgnoreCase("newrole@test.com", "newrole@test.com"))
                .thenReturn(false);

        // Role không tồn tại trong DB lúc này
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());
        
        Role newSavedRole = new Role("USER");
        when(roleRepository.save(any(Role.class))).thenReturn(newSavedRole);

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            userToSave.setId(3L);
            userToSave.setRole(newSavedRole);
            return userToSave;
        });

        UserResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("USER", response.role());
    }

    @Test
    @DisplayName("Đăng nhập thất bại do mật khẩu gửi lên dài quá giới hạn BCrypt (72 bytes)")
    void loginFailPasswordTooLong() {
        String longPassword = "a".repeat(73);
        LoginRequest request = new LoginRequest("test@test.com", longPassword);
        
        User user = new User();
        user.setPassword("hashed-password");

        when(userRepository.findFirstByUsernameIgnoreCaseOrEmailIgnoreCase("test@test.com", "test@test.com"))
                .thenReturn(Optional.of(user));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.login(request));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertEquals("Invalid username/email or password", ex.getReason());
    }
}
