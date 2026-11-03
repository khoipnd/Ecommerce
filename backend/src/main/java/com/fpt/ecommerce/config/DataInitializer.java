package com.fpt.ecommerce.config;

import com.fpt.ecommerce.entity.Account;
import com.fpt.ecommerce.entity.Role;
import com.fpt.ecommerce.repository.AccountRepository;
import com.fpt.ecommerce.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Tạo sẵn 2 role (ADMIN, USER) và 1 tài khoản admin để đăng nhập thử nếu chưa có.
 * Chỉ thêm khi thiếu, không ghi đè dữ liệu có sẵn. Đổi mật khẩu admin trước khi triển khai thật.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository, AccountRepository accountRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Role admin = roleRepository.findByName("ADMIN").orElseGet(() -> roleRepository.save(new Role("ADMIN")));
        roleRepository.findByName("USER").orElseGet(() -> roleRepository.save(new Role("USER")));

        if (accountRepository.findByUsername("admin").isEmpty()) {
            Account account = new Account();
            account.setUsername("admin");
            account.setPassword(passwordEncoder.encode("123456"));
            account.setRole(admin);
            account.setFullName("Administrator");
            account.setEmail("admin@gmail.com");
            accountRepository.save(account);
        }
    }
}
