package com.example.baliyan.service;

import com.example.baliyan.Model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

public interface UserService extends UserDetailsService {
    List<User> getAllUsers();
    Optional<User> getUserById(Long id);
    User getUserByUsername(String username);
    User saveUser(User user);
    void deleteUser(Long id);

    // Check if exists
    boolean isUsernameExists(String username);
    boolean isEmailExists(String email);
    boolean isContactNumberExists(String contactNumber);

    // Check if exists for other user (for edit operations)
    boolean isUsernameExistsForOtherUser(String username, Long userId);
    boolean isEmailExistsForOtherUser(String email, Long userId);
    boolean isContactNumberExistsForOtherUser(String contactNumber, Long userId);

    // Validation method
    String validateUserData(User user, Long excludeUserId);

    // Admin approval methods
    List<User> getPendingApprovalUsers();
    List<User> getApprovedUsers();
    void approveUser(Long userId);
    void rejectUser(Long userId);
    void toggleUserStatus(Long userId);
    void updateUser(Long id, User user);


    User findByUsername(String name);
}