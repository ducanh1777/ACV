package com.acv.assetmanagement.config;

import com.acv.assetmanagement.model.User;
import com.acv.assetmanagement.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class PasswordChangeInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;

    public PasswordChangeInterceptor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            String username = auth.getName();
            Optional<User> userOpt = userRepository.findByUsername(username);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String requestUri = request.getRequestURI();

                // If user hasn't changed password and is not trying to access change-password
                // page or static resources
                if (!user.isPasswordChanged() &&
                        !requestUri.equals("/change-password") &&
                        !requestUri.equals("/logout") &&
                        !requestUri.startsWith("/css/") &&
                        !requestUri.startsWith("/js/") &&
                        !requestUri.startsWith("/images/")) {

                    response.sendRedirect("/change-password");
                    return false;
                }
            }
        }

        return true;
    }
}
