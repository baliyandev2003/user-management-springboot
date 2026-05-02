package com.example.baliyan.Controller;

import com.example.baliyan.Model.Role;
import com.example.baliyan.Model.User;
import com.example.baliyan.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String home() {
        return "redirect:/select-role";
    }

    @GetMapping("/select-role")
    public String showRoleSelection() {
        return "auth/role-selection";  // Added "auth/" prefix
    }

    @GetMapping("/auth/role-selection")
    public String showAuthRoleSelection() {
        return "auth/role-selection";  // Added "auth/" prefix
    }

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout,
                                @RequestParam(value = "pending_approval", required = false) String pendingApproval,
                                Model model) {

        logger.info("Login page accessed with params - error: {}, logout: {}, pending_approval: {}",
                error, logout, pendingApproval);

        if (error != null) {
            model.addAttribute("error", "Invalid username or password!");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }
        if (pendingApproval != null) {
            model.addAttribute("message", "Your account is pending admin approval. You will be notified once approved.");
        }

        return "auth/login";  // Added "auth/" prefix
    }

    @GetMapping("/signup")
    public String showSignupForm(@RequestParam(value = "role", defaultValue = "USER") String role,
                                 Model model) {
        logger.info("Signup page accessed for role: {}", role);

        User user = new User();
        try {
            Role userRole = Role.valueOf(role.toUpperCase());
            user.setRole(userRole);
            model.addAttribute("user", user);
            model.addAttribute("selectedRole", role);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid role specified: {}", role);
            model.addAttribute("error", "Invalid role specified");
            return "redirect:/select-role";
        }
        return "auth/signup";  // Added "auth/" prefix
    }

    @GetMapping("/register")
    public String showRegisterPage(@RequestParam(value = "role", required = false) String role,
                                   Model model) {
        if (role != null) {
            model.addAttribute("selectedRole", role);
        }
        return "auth/register";  // Added "auth/" prefix
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        logger.warn("Access denied page accessed");
        return "access-denied";  // This stays in root templates
    }

    @GetMapping("/login-success")
    public String loginSuccess() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        logger.info("Login success for user: {}", username);

        User user = userService.getUserByUsername(username);

        if (user != null) {
            if (user.getRole() == Role.ADMIN ) {
                logger.info("Redirecting admin to dashboard");
                return "redirect:/admin/dashboard";
            } else {
                logger.info("Redirecting user to dashboard");
                return "redirect:/user/dashboard";
            }
        }
        logger.error("User not found after login: {}", username);
        return "redirect:/login?error=true";
    }

    @PostMapping("/signup")
    public String processSignup(@Valid @ModelAttribute User user,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        try {
            logger.info("Signup attempt for: {} with role: {}", user.getUsername(), user.getRole());

            if (bindingResult.hasErrors()) {
                logger.warn("Signup validation errors: {}", bindingResult.getAllErrors());
                model.addAttribute("selectedRole", user.getRole().name());
                return "auth/signup";  // Added "auth/" prefix
            }

            String validationError = userService.validateUserData(user, null);
            if (validationError != null) {
                logger.warn("Signup validation error: {}", validationError);
                model.addAttribute("error", validationError);
                model.addAttribute("selectedRole", user.getRole().name());
                return "auth/signup";  // Added "auth/" prefix
            }

            User savedUser = userService.saveUser(user);
            logger.info("User saved successfully: {} with role: {}", savedUser.getUsername(), savedUser.getRole());

            if (savedUser.getRole() == Role.ADMIN ) {
                redirectAttributes.addFlashAttribute("message", "Admin account created successfully! You can now login.");
                logger.info("Admin account created: {}", savedUser.getUsername());
                return "redirect:/login";
            } else {
                redirectAttributes.addFlashAttribute("message", "Registration successful! Please wait for admin approval.");
                logger.info("User account created (pending approval): {}", savedUser.getUsername());
                return "redirect:/login?pending_approval=true";
            }

        } catch (Exception e) {
            logger.error("Error during signup: {}", e.getMessage(), e);
            model.addAttribute("error", "Error during signup: " + e.getMessage());
            model.addAttribute("selectedRole", user.getRole().name());
            return "auth/signup";  // Added "auth/" prefix
        }
    }
}