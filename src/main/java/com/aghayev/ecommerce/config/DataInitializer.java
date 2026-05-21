package com.aghayev.ecommerce.config;

import com.aghayev.ecommerce.entity.User;
import com.aghayev.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail("admin@test.com")) {
            log.info("dataInitializer status=SKIP reason=admin_already_exists email=admin@test.com");
            return;
        }

        User admin = new User();
        admin.setEmail("admin@test.com");
        admin.setPassword(passwordEncoder.encode("Admin123!"));
        admin.setRole(User.Role.ADMIN);

        userRepository.save(admin);
        log.info("dataInitializer status=CREATED email=admin@test.com role=ADMIN");
    }
}
