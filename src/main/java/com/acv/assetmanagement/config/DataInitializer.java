package com.acv.assetmanagement.config;

import com.acv.assetmanagement.model.Role;
import com.acv.assetmanagement.model.User;
import com.acv.assetmanagement.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@acv.vn");
            admin.setRoles(new HashSet<>(Collections.singletonList(Role.ROLE_ADMIN)));
            userRepository.save(admin);
            System.out.println("Initial admin user created: admin/admin123");
        }
    }
}
