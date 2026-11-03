package com.fpt.authservice.config;

import com.fpt.authservice.entity.Role;
import com.fpt.authservice.entity.User;
import com.fpt.authservice.repository.RoleRepository;
import com.fpt.authservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Chạy một lần khi ứng dụng khởi động: tạo role ADMIN, USER và tài khoản admin nếu chưa có.
 * Chỉ thêm khi thiếu, không ghi đè dữ liệu có sẵn.
 */
@Component
public class DataInitializer implements CommandLineRunner {


    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean enabled;
    private final String adminPassword;

    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${app.seed.enabled:true}") boolean enabled,
                           @Value("${app.seed.admin-password}") String adminPassword) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (!enabled) {
            return;
        }
        Role admin = roleRepository.findByName("ADMIN").orElseGet(() -> roleRepository.save(new Role("ADMIN")));
        roleRepository.findByName("USER").orElseGet(() -> roleRepository.save(new Role("USER")));

        if (userRepository.findFirstByUsernameIgnoreCaseOrEmailIgnoreCase("admin", "admin@gmail.com").isEmpty()) {
            User user = new User();
            user.setUsername("admin");
            user.setEmail("admin@gmail.com");
            user.setPassword(passwordEncoder.encode(adminPassword));
            user.setFullName("Administrator");
            user.setRole(admin);
            userRepository.save(user);
        }
    }
}
