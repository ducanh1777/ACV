package com.acv.assetmanagement.controller;

import com.acv.assetmanagement.model.Role;
import com.acv.assetmanagement.model.User;
import com.acv.assetmanagement.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.HashSet;

@Controller
public class MainController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public MainController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "Bảng điều khiển - ACV Asset Management");
        return "index";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forgot-password";
    }

    @GetMapping("/admin/employees")
    public String employeeList(Model model) {
        model.addAttribute("title", "Danh sách nhân viên - ACV Admin");
        model.addAttribute("users", userRepository.findAll());
        return "admin/employee-list";
    }

    @GetMapping("/admin/register-employee")
    public String registerEmployeeForm(Model model) {
        model.addAttribute("title", "Đăng ký nhân viên - ACV Admin");
        return "admin/register-employee";
    }

    @PostMapping("/admin/register-employee")
    public String registerEmployee(@RequestParam String username,
            @RequestParam String password,
            @RequestParam String email,
            @RequestParam String role,
            Model model) {
        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Username already exists");
            return "admin/register-employee";
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setRoles(new HashSet<>(Collections.singletonList(Role.valueOf(role))));
        userRepository.save(user);

        model.addAttribute("success", "Nhân viên đã được đăng ký thành công!");
        return "redirect:/admin/employees?success=registered";
    }
}
