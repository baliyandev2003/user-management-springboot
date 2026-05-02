package com.example.baliyan.Controller;

import com.example.baliyan.Model.PasswordResetToken;
import com.example.baliyan.Model.User;
import com.example.baliyan.repository.PasswordResetTokenRepository;
import com.example.baliyan.repository.UserRepository;
import com.example.baliyan.service.EmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
public class ForgotPasswordController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /* ==============================
       SHOW EMAIL INPUT PAGE
       ============================== */
    @GetMapping("/forgot-password")
    public String showForgotPasswordPage() {
        return "auth/forgot-password";
    }

    /* ==============================
       SEND OTP
       ============================== */
    @PostMapping("/forgot-password")
    public String sendOtp(@RequestParam String email,
                          HttpSession session,
                          Model model) {

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            model.addAttribute("error", "No such email registered");
            return "auth/forgot-password";
        }

        /* GENERATE 6 DIGIT OTP */
        String otp = String.valueOf((int)(Math.random() * 900000) + 100000);

        /* STORE IN SESSION */
        session.setAttribute("otp", otp);
        session.setAttribute("email", email);

        /* SEND EMAIL */
        emailService.sendOtpEmail(email, otp);

        return "auth/verify-otp";
    }

    /* ==============================
       VERIFY OTP
       ============================== */
    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String otp,
                            HttpSession session,
                            Model model) {

        String sessionOtp = (String) session.getAttribute("otp");

        if (sessionOtp == null || !sessionOtp.equals(otp)) {
            model.addAttribute("error", "Invalid OTP");
            return "auth/verify-otp";
        }

        return "auth/reset-password";
    }

    /* ==============================
       RESET PASSWORD
       ============================== */
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String password,
                                HttpSession session) {

        String email = (String) session.getAttribute("email");

        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
        }

        session.invalidate();

        return "redirect:/login?resetSuccess";
    }
}
