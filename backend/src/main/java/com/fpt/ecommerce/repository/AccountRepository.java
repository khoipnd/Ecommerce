package com.fpt.ecommerce.repository;

import com.fpt.ecommerce.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByUsernameIgnoreCaseOrEmailIgnoreCase(String username, String email);

    Optional<Account> findByUsername(String username);

    Optional<Account> findFirstByUsernameOrEmail(String username, String email);
}