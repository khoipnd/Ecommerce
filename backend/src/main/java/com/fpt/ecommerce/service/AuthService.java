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
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class AuthService {

    private static final String INVALID_CREDENTIALS = "Invalid username/email or password";

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;

    public AuthService(
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RoleRepository roleRepository
    ) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public AccountInfo register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (accountRepository.existsByUsernameIgnoreCaseOrEmailIgnoreCase(email, email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already registered"
            );
        }

        Account account = new Account();
        account.setUsername(email);
        account.setEmail(email);

        // Đăng ký REST chỉ lưu mật khẩu đã mã hóa BCrypt.
        account.setPassword(passwordEncoder.encode(request.password()));

        // Không nhận quyền admin từ client.
        account.setRole(
                roleRepository.findByName("USER")
                        .orElseGet(() -> roleRepository.save(new Role("USER")))
        );

        accountRepository.save(account);

        return new AccountInfo(
                account.getId(),
                account.getUsername(),
                account.getFullName(),
                email,
                "USER"
        );
    }

    public LoginResponse login(LoginRequest request) {
        String identifier = request.getIdentifier().trim();

        // Cùng một thông báo lỗi cho "không tồn tại" và "sai mật khẩu" để không lộ tài khoản nào có thật.
        Account account = accountRepository
                .findFirstByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                INVALID_CREDENTIALS
                        )
                );

        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    INVALID_CREDENTIALS
            );
        }

        String role = account.getRole().getName();
        String token = jwtService.generateToken(account.getId(), role);

        return new LoginResponse(
                token,
                jwtService.getExpirationSeconds(),
                account.getId(),
                account.getUsername(),
                account.getFullName(),
                account.getEmail(),
                role
        );
    }

    /** Lấy thông tin tài khoản từ header "Authorization: Bearer <token>". */
    public AccountInfo currentAccount(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Missing or invalid Authorization header"
            );
        }

        JwtService.Claims claims = jwtService
                .parse(authorizationHeader.substring(7).trim())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Invalid or expired token"
                        )
                );

        Account account = accountRepository
                .findById(claims.accountId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Account no longer exists"
                        )
                );

        return new AccountInfo(
                account.getId(),
                account.getUsername(),
                account.getFullName(),
                account.getEmail(),
                account.getRole().getName()
        );
    }
}