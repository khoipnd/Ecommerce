package com.fpt.ecommerce.service;

import com.fpt.ecommerce.dto.AccountInfo;
import com.fpt.ecommerce.dto.LoginRequest;
import com.fpt.ecommerce.dto.LoginResponse;
import com.fpt.ecommerce.dto.RegisterRequest;
import com.fpt.ecommerce.entity.Account;
import com.fpt.ecommerce.entity.Role;
import com.fpt.ecommerce.repository.AccountRepository;
import com.fpt.ecommerce.repository.RoleRepository;
import com.fpt.ecommerce.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AccountRepository accountRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtService jwtService;
    
    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private AuthService authService;

    private Account account;
    private Role role;

    @BeforeEach
    void setUp() {
        // Khởi tạo dữ liệu quyền hạn (Role) mẫu
        role = new Role("USER");
        
        // Khởi tạo tài khoản mẫu dùng cho các test
        account = new Account();
        account.setId(1L);
        account.setUsername("test@example.com");
        account.setEmail("test@example.com");
        account.setPassword("encoded_password");
        account.setRole(role);
    }

    @Test
    @DisplayName("Register - Success")
    void registerSuccess() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password");
        
        // Giả lập email chưa tồn tại trong hệ thống
        when(accountRepository.existsByUsernameIgnoreCaseOrEmailIgnoreCase(anyString(), anyString())).thenReturn(false);
        // Giả lập mã hoá mật khẩu
        when(passwordEncoder.encode("password")).thenReturn("encoded_password");
        // Giả lập gán Role mặc định
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
        // Giả lập việc lưu xuống DB
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        
        // Gọi hàm đăng ký
        AccountInfo result = authService.register(request);
        
        // Kiểm tra đăng ký thành công và trả về thông tin đúng
        assertNotNull(result);
        assertEquals("test@example.com", result.email());
        assertEquals("USER", result.role());
    }

    @Test
    @DisplayName("Register - Email already exists")
    void registerEmailExists() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password");
        
        // Giả lập email đã bị đăng ký trước đó
        when(accountRepository.existsByUsernameIgnoreCaseOrEmailIgnoreCase(anyString(), anyString())).thenReturn(true);
        
        // Kiểm tra lỗi CONFLICT do trùng email
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.register(request));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        
        // Đảm bảo không gọi hành động lưu xuống DB
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("Login - Success")
    void loginSuccess() {
        LoginRequest request = new LoginRequest();
        request.setIdentifier("test@example.com");
        request.setPassword("password");
        
        // Giả lập tìm thấy tài khoản dựa trên identifier (username/email)
        when(accountRepository.findFirstByUsernameOrEmail("test@example.com", "test@example.com")).thenReturn(Optional.of(account));
        // Giả lập so sánh mật khẩu chính xác
        when(passwordEncoder.matches("password", "encoded_password")).thenReturn(true);
        // Giả lập sinh JWT token
        when(jwtService.generateToken(1L, "USER")).thenReturn("token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);
        
        // Gọi hàm đăng nhập
        LoginResponse result = authService.login(request);
        
        // Kiểm tra token và thời hạn được trả về
        assertNotNull(result);
        assertEquals("token", result.getToken());
        assertEquals(3600L, result.getExpiresInSeconds());
    }

    @Test
    @DisplayName("Login - Account Not Found")
    void loginAccountNotFound() {
        LoginRequest request = new LoginRequest();
        request.setIdentifier("test@example.com");
        request.setPassword("password");
        
        // Giả lập không tìm thấy tài khoản trong hệ thống
        when(accountRepository.findFirstByUsernameOrEmail("test@example.com", "test@example.com")).thenReturn(Optional.empty());
        
        // Kỳ vọng lỗi UNAUTHORIZED (401)
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.login(request));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    @DisplayName("Login - Wrong Password")
    void loginWrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setIdentifier("test@example.com");
        request.setPassword("wrongpassword");
        
        // Tìm thấy tài khoản nhưng mật khẩu sai
        when(accountRepository.findFirstByUsernameOrEmail("test@example.com", "test@example.com")).thenReturn(Optional.of(account));
        // Hàm matches() trả về false (không khớp)
        when(passwordEncoder.matches("wrongpassword", "encoded_password")).thenReturn(false);
        
        // Kỳ vọng lỗi UNAUTHORIZED (401)
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.login(request));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    @DisplayName("Current Account - Success")
    void currentAccountSuccess() {
        // Giả lập giải mã thành công claims từ Token
        when(jwtService.parse("token")).thenReturn(Optional.of(new JwtService.Claims(1L, "USER")));
        // Giả lập lấy thông tin tài khoản bằng ID trong token
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        
        // Lấy thông tin tài khoản hiện tại
        AccountInfo result = authService.currentAccount("Bearer token");
        
        assertNotNull(result);
        assertEquals("test@example.com", result.email());
    }

    @Test
    @DisplayName("Current Account - Invalid Header")
    void currentAccountInvalidHeader() {
        // Gửi token bị sai định dạng (thiếu chữ Bearer)
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.currentAccount("token"));
        // Kỳ vọng trả về mã lỗi UNAUTHORIZED
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }
}
