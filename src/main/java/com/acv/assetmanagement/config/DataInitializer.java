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
    private final com.acv.assetmanagement.repository.DeviceRepository deviceRepository;
    private final com.acv.assetmanagement.repository.DeviceLogRepository deviceLogRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
            com.acv.assetmanagement.repository.DeviceRepository deviceRepository,
            com.acv.assetmanagement.repository.DeviceLogRepository deviceLogRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.deviceRepository = deviceRepository;
        this.deviceLogRepository = deviceLogRepository;
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
            admin.setPasswordChanged(true);
            userRepository.save(admin);
            System.out.println("Initial admin user created: admin/admin123");
        }

        // Migrate existing logs to Vietnamese
        java.util.List<com.acv.assetmanagement.model.DeviceLog> allLogs = deviceLogRepository.findAll();
        for (com.acv.assetmanagement.model.DeviceLog log : allLogs) {
            boolean updated = false;
            if ("CREATE".equals(log.getAction())) {
                log.setAction("Tạo mới");
                updated = true;
            } else if ("UPDATE".equals(log.getAction())) {
                log.setAction("Cập nhật");
                updated = true;
            } else if ("DELETE".equals(log.getAction())) {
                log.setAction("Xóa");
                updated = true;
            } else if ("CHECKOUT".equals(log.getAction())) {
                log.setAction("Cấp phát");
                updated = true;
            } else if ("CHECKIN".equals(log.getAction())) {
                log.setAction("Nhập kho");
                updated = true;
            }

            if ("Cập nhật thông tin thiết bị".equals(log.getDetails()) || "Update device information".equalsIgnoreCase(log.getDetails()) || "Cập nhật thông tin chi tiết thiết bị".equals(log.getDetails())) {
                log.setDetails("(Lịch sử cũ) Cập nhật thông tin thiết bị");
                updated = true;
            }
            if ("Tạo mới thiết bị".equals(log.getDetails()) || "Create new device".equalsIgnoreCase(log.getDetails()) || "Tạo mới thiết bị vào hệ thống".equals(log.getDetails())) {
                log.setDetails("(Lịch sử cũ) Tạo mới thiết bị");
                updated = true;
            }

            if (updated) {
                deviceLogRepository.save(log);
            }
        }

        // Populate missing fields for existing devices
        java.util.List<com.acv.assetmanagement.model.Device> allDevices = deviceRepository.findAll();
        for (com.acv.assetmanagement.model.Device d : allDevices) {
            boolean updated = false;
            if (d.getTerminal() == null || d.getTerminal().isEmpty()) {
                d.setTerminal("T1");
                updated = true;
            }
            if (d.getDeviceSystem() == null || d.getDeviceSystem().isEmpty()) {
                d.setDeviceSystem("FIDS");
                updated = true;
            }
            if (updated) {
                deviceRepository.save(d);
            }
        }
    }
}
