package com.example.baliyan.Controller.user;

import com.example.baliyan.Model.User;
import com.example.baliyan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    public static String UPLOAD_DIRECTORY = System.getProperty("user.dir") + "/uploads";

    // ================= COMMON USER DATA =================
    private void addUserAttributes(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return;
        }

        String username = auth.getName();
        User user = userService.getUserByUsername(username);

        if (user != null) {
            model.addAttribute("user", user);
        }

        model.addAttribute("title", "User Dashboard");
    }

    // ================= DASHBOARD =================
    @GetMapping("/dashboard")
    public String userDashboard(Model model) {

        addUserAttributes(model);

        User user = (User) model.getAttribute("user");

        if (user == null) {
            return "redirect:/login?error=user_not_found";
        }

        model.addAttribute("loginTime",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));

        model.addAttribute("todayDate",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")));

        model.addAttribute("title", "Dashboard");

        return "user/dashboard";
    }

    // ================= PROFILE =================
    @GetMapping("/profile")
    public String showUserProfile(Model model) {
        addUserAttributes(model);
        model.addAttribute("title", "My Profile");
        return "user/profile";
    }

    @PostMapping("/profile")
    public String updateUserProfile(@Valid @ModelAttribute("user") User formUser,
                                    BindingResult bindingResult,
                                    @RequestParam(value = "profileImage", required = false) MultipartFile file,
                                    RedirectAttributes redirectAttributes,
                                    Model model) {

        addUserAttributes(model);

        User existingUser = (User) model.getAttribute("user");

        if (existingUser == null) {
            return "redirect:/login?error=user_not_found";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("title", "My Profile");
            return "user/profile";
        }

        try {

            String validationError =
                    userService.validateUserData(formUser, existingUser.getId());

            if (validationError != null) {
                redirectAttributes.addFlashAttribute("errorMessage", validationError);
                return "redirect:/user/profile";
            }

            // File Upload
            if (file != null && !file.isEmpty()) {
                String fileName = saveUploadedFile(file, existingUser.getId());
                existingUser.setProfilePhoto(fileName);
            }

            // Update fields
            existingUser.setName(formUser.getName());
            existingUser.setEmail(formUser.getEmail());
            existingUser.setAge(formUser.getAge());
            existingUser.setContactNumber(formUser.getContactNumber());
            existingUser.setAddress(formUser.getAddress());
            existingUser.setBio(formUser.getBio());

            if (formUser.getPassword() != null &&
                    !formUser.getPassword().trim().isEmpty()) {

                existingUser.setPassword(formUser.getPassword());
            }

            userService.saveUser(existingUser);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Profile updated successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error updating profile: " + e.getMessage());
            return "redirect:/user/profile";
        }

        return "redirect:/user/dashboard";
    }

    // ================= STATIC PAGES =================
    @GetMapping("/about")
    public String aboutPage(Model model) {
        addUserAttributes(model);
        model.addAttribute("title", "About Us");
        return "user/about";
    }

    @GetMapping("/settings")
    public String settingsPage(Model model) {
        addUserAttributes(model);
        model.addAttribute("title", "Settings");
        return "user/settings";
    }

    @GetMapping("/help")
    public String helpPage(Model model) {
        addUserAttributes(model);
        model.addAttribute("title", "Help & Support");
        return "user/help";
    }

    // ================= FILE UPLOAD =================
    private String saveUploadedFile(MultipartFile file, Long userId) throws IOException {

        Path uploadPath = Paths.get(UPLOAD_DIRECTORY);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IOException("Invalid file name");
        }

        String fileExtension =
                originalFilename.substring(originalFilename.lastIndexOf("."));

        String fileName =
                "user_" + userId + "_profile" + fileExtension;

        Path filePath = uploadPath.resolve(fileName);

        Files.copy(file.getInputStream(), filePath);

        return "/uploads/" + fileName;
    }
}

