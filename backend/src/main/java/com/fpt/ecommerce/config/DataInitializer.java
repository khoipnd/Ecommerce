package com.fpt.ecommerce.config;

import com.fpt.ecommerce.entity.Account;
import com.fpt.ecommerce.entity.Role;
import com.fpt.ecommerce.repository.AccountRepository;
import com.fpt.ecommerce.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Tự động tạo role (ADMIN, USER) và đồng bộ tài khoản admin theo mật khẩu cấu hình.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminPassword;

    public DataInitializer(RoleRepository roleRepository,
                           AccountRepository accountRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${app.admin.password:${ADMIN_PASSWORD:123456}}") String adminPassword) {
        this.roleRepository = roleRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        Role admin = roleRepository.findByName("ADMIN").orElseGet(() -> roleRepository.save(new Role("ADMIN")));
        roleRepository.findByName("USER").orElseGet(() -> roleRepository.save(new Role("USER")));

        Account account = accountRepository.findByUsername("admin").orElseGet(() -> {
            Account acc = new Account();
            acc.setUsername("admin");
            acc.setRole(admin);
            acc.setFullName("Administrator");
            acc.setEmail("admin@gmail.com");
            return acc;
        });

        // Tự động đồng bộ mật khẩu admin theo cấu hình
        account.setPassword(passwordEncoder.encode(adminPassword));
        accountRepository.save(account);
    }
}
