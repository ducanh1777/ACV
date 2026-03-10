package com.acv.assetmanagement.controller;

import com.acv.assetmanagement.model.Role;
import com.acv.assetmanagement.model.User;
import com.acv.assetmanagement.repository.UserRepository;
import com.acv.assetmanagement.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.HashSet;

@Controller
public class MainController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public MainController(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @ModelAttribute
    public void addAttributes(HttpServletRequest request, Model model) {
        model.addAttribute("currentUri", request.getRequestURI());
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

        // Send confirmation email
        emailService.sendEmployeeCredentials(email, username, password);

        model.addAttribute("success", "Nhân viên đã được đăng ký thành công!");
        return "redirect:/admin/employees?success=registered";
    }

    @GetMapping("/change-password")
    public String changePasswordForm(Model model) {
        model.addAttribute("title", "Đổi mật khẩu - ACV");
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String oldPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            HttpServletRequest request,
            Model model) {
        String username = request.getUserPrincipal().getName();
        User user = userRepository.findByUsername(username).orElseThrow();

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            model.addAttribute("error", "Mật khẩu cũ không chính xác");
            return "change-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu mới không khớp");
            return "change-password";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChanged(true);
        userRepository.save(user);

        model.addAttribute("success", "Đổi mật khẩu thành công!");
        return "redirect:/?success=password_changed";
    }
}
