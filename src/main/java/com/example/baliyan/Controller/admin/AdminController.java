package com.example.baliyan.Controller.admin;

import com.example.baliyan.Model.User;
import com.example.baliyan.Model.Role;
import com.example.baliyan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    // Helper method to add common admin attributes
    private void addAdminAttributes(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User currentUser = userService.getUserByUsername(username);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("title", "Admin Panel");
    }

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        addAdminAttributes(model);

        try {
            List<User> allUsers = userService.getAllUsers();
            List<User> pendingUsers = userService.getPendingApprovalUsers();
            List<User> approvedUsers = userService.getApprovedUsers();
            List<User> activeUsers = userService.getAllUsers().stream()
                    .filter(User::isActive)
                    .toList();

            model.addAttribute("totalUsers", allUsers.size());
            model.addAttribute("pendingUsers", pendingUsers);
            model.addAttribute("approvedUsers", approvedUsers);
            model.addAttribute("activeUsers", activeUsers);
//            model.addAttribute("pendingCount", pendingUsers.size());
            model.addAttribute("pendingCount", pendingUsers.size());

            return "admin/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
            return "error";
        }
    }

    // VIEW ALL USERS PAGE
    @GetMapping("/users")
    public String viewAllUsers(@RequestParam(required = false) String status,
                               @RequestParam(required = false) String search,
                               Model model) {
        addAdminAttributes(model);

        try {
            List<User> users;

            if (search != null && !search.trim().isEmpty()) {
                users = filterUsersBySearch(userService.getAllUsers(), search.trim());
            } else if (status != null && !status.isEmpty()) {
                switch (status.toLowerCase()) {
                    case "pending":
                        users = userService.getPendingApprovalUsers();
                        break;
                    case "approved":
                        users = userService.getApprovedUsers();
                        break;
                    case "active":
                        users = userService.getAllUsers().stream()
                                .filter(User::isActive)
                                .toList();
                        break;
                    case "inactive":
                        users = userService.getAllUsers().stream()
                                .filter(user -> !user.isActive())
                                .toList();
                        break;
                    default:
                        users = userService.getAllUsers();
                }
            } else {
                users = userService.getAllUsers();
            }

            model.addAttribute("users", users);
            model.addAttribute("totalUsers", users.size());
            model.addAttribute("status", status);
            model.addAttribute("search", search);

            return "admin/users";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading users: " + e.getMessage());
            return "error";
        }
    }


    // PENDING APPROVALS PAGE
    @GetMapping("/pending-approvals")
    public String pendingApprovals(Model model) {
        addAdminAttributes(model);

        try {
            List<User> pendingUsers = userService.getPendingApprovalUsers();
            model.addAttribute("pendingUsers", pendingUsers);
            model.addAttribute("totalPending", pendingUsers.size());

            return "admin/pending-approvals";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading pending approvals: " + e.getMessage());
            return "error";
        }
    }

    // APPROVE USER - Redirect to dashboard
    @PostMapping("/approve-user/{id}")
    public String approveUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.approveUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User approved successfully!");
            return "redirect:/admin/dashboard"; // Redirect to dashboard
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error approving user: " + e.getMessage());
            return "redirect:/admin/pending-approvals"; // Stay on same page if error
        }
    }

    // REJECT USER - Stay on same page
    @PostMapping("/reject-user/{id}")
    public String rejectUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.rejectUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User rejected successfully!");
            return "redirect:/admin/pending-approvals"; // Stay on same page
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error rejecting user: " + e.getMessage());
            return "redirect:/admin/pending-approvals"; // Stay on same page
        }
    }

    // TOGGLE USER STATUS (Active/Inactive)
    @PostMapping("/toggle-status/{id}")
    public String toggleUserStatus(@PathVariable Long id,
                                   @RequestParam(required = false) String redirectTo,
                                   RedirectAttributes redirectAttributes) {
        try {
            userService.toggleUserStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "User status updated successfully!");

            // Determine where to redirect
            if ("pending".equals(redirectTo)) {
                return "redirect:/admin/pending-approvals";
            } else if ("users".equals(redirectTo)) {
                return "redirect:/admin/users";
            } else {
                return "redirect:/admin/dashboard";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating user status: " + e.getMessage());
            return "redirect:/admin/users";
        }
    }




    // DELETE USER
    @PostMapping("/delete-user/{id}")

    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // ADD USER METHODS (Your existing code)
    @GetMapping("/add-user")
    public String showAddUserForm(@RequestParam(value = "role", required = false) String role,
                                  Model model) {
        addAdminAttributes(model);

        // If no role is selected, show role selection page
        if (role == null || role.isEmpty()) {
            model.addAttribute("title", "Select User Role");
            return "admin/select-role";
        }

        // Create new user object
        User user = new User();

        // Set role based on parameter
        if ("USER".equalsIgnoreCase(role)) {
            user.setRole(Role.USER);
        } else if ("ADMIN".equalsIgnoreCase(role)) {
            user.setRole(Role.ADMIN);
        } else {
            // If invalid role, go back to role selection
            return "redirect:/admin/add-user";
        }

        model.addAttribute("user", user);
        model.addAttribute("title", "Add New " + role + " User");
        model.addAttribute("selectedRole", role);

        return "admin/add-user";
    }

    @PostMapping("/add-user")
    public String addUser(@Valid @ModelAttribute("user") User user,
                          BindingResult bindingResult,
                          @RequestParam(value = "selectedRole", required = false) String selectedRole,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        addAdminAttributes(model);

        if (bindingResult.hasErrors()) {
            model.addAttribute("title", "Add New User");
            model.addAttribute("selectedRole", selectedRole);
            return "admin/add-user";
        }

        try {
            String validationError = userService.validateUserData(user, null);
            if (validationError != null) {
                model.addAttribute("errorMessage", validationError);
                model.addAttribute("title", "Add New User");
                model.addAttribute("selectedRole", selectedRole);
                return "admin/add-user";
            }

            // Auto-approve admin users
            if (user.getRole() == Role.ADMIN ) {
                user.setApproved(true);
            }

            // Set default values
            user.setActive(true);

            // Save the user
            userService.saveUser(user);

            redirectAttributes.addFlashAttribute("successMessage",
                    user.getRole() + " user '" + user.getUsername() + "' added successfully! " +
                            (user.isApproved() ? "User is approved and can login immediately." :
                                    "User needs admin approval to login."));

            return "redirect:/admin/dashboard";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error adding user: " + e.getMessage());
            model.addAttribute("title", "Add New User");
            model.addAttribute("selectedRole", selectedRole);
            return "admin/add-user";
        }
    }

    @GetMapping("/admin-edit-user/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        addAdminAttributes(model);

        User user = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", user);
        model.addAttribute("title", "Edit User");

        return "admin/admin-edit-user";
    }

    @PostMapping("/admin-edit-user/{id}")
    public String updateUser(@PathVariable Long id,
                             @Valid @ModelAttribute("user") User user,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {

        addAdminAttributes(model);

        if (bindingResult.hasErrors()) {
            model.addAttribute("title", "Edit User");
            return "admin/admin-edit-user";
        }

        try {
            userService.updateUser(id, user);
            redirectAttributes.addFlashAttribute("successMessage", "User updated successfully!");
            return "redirect:/admin/users";

        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("title", "Edit User");
            return "admin/admin-edit-user";
        }
    }


    // Helper method for search (simple implementation)
    private List<User> filterUsersBySearch(List<User> users, String searchTerm) {
        String lowerSearch = searchTerm.toLowerCase();
        return users.stream()
                .filter(user ->
                        user.getUsername().toLowerCase().contains(lowerSearch) ||
                                user.getEmail().toLowerCase().contains(lowerSearch) ||
                                user.getName().toLowerCase().contains(lowerSearch) ||
                                (user.getContactNumber() != null && user.getContactNumber().contains(lowerSearch))
                )
                .toList();
    }
}