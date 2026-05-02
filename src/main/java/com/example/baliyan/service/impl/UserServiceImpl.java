package com.example.baliyan.service.impl;

import com.example.baliyan.Model.Role;
import com.example.baliyan.Model.User;
import com.example.baliyan.repository.UserRepository;
import com.example.baliyan.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Attempting to load user by username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("User not found with username: {}", username);
                    return new UsernameNotFoundException("User not found with username: " + username);
                });

        logger.info("Found user: {}, Role: {}, Approved: {}, Active: {}",
                user.getUsername(), user.getRole(), user.isApproved(), user.isActive());

        if (!user.isActive()) {
            logger.error("User account is deactivated: {}", username);
            throw new UsernameNotFoundException("User account is deactivated: " + username);
        }

        if (user.getRole() == Role.USER && !user.isApproved()) {
            logger.error("User account not approved yet: {}", username);
            throw new UsernameNotFoundException("User account not approved yet: " + username);
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }

    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        String role = "ROLE_" + user.getRole().name();
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    @Override
    public boolean isUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean isContactNumberExists(String contactNumber) {
        return userRepository.existsByContactNumber(contactNumber);
    }

    @Override
    public boolean isUsernameExistsForOtherUser(String username, Long userId) {
        return userRepository.existsByUsernameAndIdNot(username, userId);
    }

    @Override
    public boolean isEmailExistsForOtherUser(String email, Long userId) {
        return userRepository.existsByEmailAndIdNot(email, userId);
    }

    @Override
    public boolean isContactNumberExistsForOtherUser(String contactNumber, Long userId) {
        return userRepository.existsByContactNumberAndIdNot(contactNumber, userId);
    }

    @Override
    public String validateUserData(User user, Long excludeUserId) {
        if (excludeUserId != null) {
            if (isUsernameExistsForOtherUser(user.getUsername(), excludeUserId)) {
                return "Username already exists!";
            }
        } else {
            if (isUsernameExists(user.getUsername())) {
                return "Username already exists!";
            }
        }

        if (excludeUserId != null) {
            if (isEmailExistsForOtherUser(user.getEmail(), excludeUserId)) {
                return "Email already exists!";
            }
        } else {
            if (isEmailExists(user.getEmail())) {
                return "Email already exists!";
            }
        }

        if (user.getContactNumber() != null && !user.getContactNumber().isEmpty()) {
            if (excludeUserId != null) {
                if (isContactNumberExistsForOtherUser(user.getContactNumber(), excludeUserId)) {
                    return "Contact number already exists!";
                }
            } else {
                if (isContactNumberExists(user.getContactNumber())) {
                    return "Contact number already exists!";
                }
            }
        }

        return null;
    }

    @Override
    public User saveUser(User user) {
        if (user.getId() != null) {
            Optional<User> existingUser = userRepository.findById(user.getId());
            if (existingUser.isPresent()) {
                user.setCreatedAt(existingUser.get().getCreatedAt());
            }
            user.setUpdatedAt(LocalDateTime.now());
        } else {
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            if (user.getRole() == Role.ADMIN ) {
                user.setApproved(true);
                logger.info("Admin user auto-approved: {}", user.getUsername());
            } else {
                user.setApproved(false);
                logger.info("Regular user waiting for approval: {}", user.getUsername());
            }

            user.setActive(true);
        }

        String password = user.getPassword();
        if (password != null && !password.trim().isEmpty() &&
                !password.startsWith("$2a$")) {
            String hashedPassword = passwordEncoder.encode(password);
            user.setPassword(hashedPassword);
        } else if (user.getId() != null && (password == null || password.trim().isEmpty())) {
            User existingUser = userRepository.findById(user.getId()).orElse(null);
            if (existingUser != null) {
                user.setPassword(existingUser.getPassword());
            }
        }

        User savedUser = userRepository.save(user);
        logger.info("User saved successfully with ID: {}", savedUser.getId());
        return savedUser;
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
        logger.info("User deleted with ID: {}", id);
    }

    @Override
    public List<User> getPendingApprovalUsers() {
        return userRepository.findByApprovedFalse();
    }

    @Override
    public List<User> getApprovedUsers() {
        return userRepository.findByApprovedTrue();
    }

    @Override
    public void approveUser(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setApproved(true);
            user.setActive(true); // Also activate when approving
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            logger.info("User approved: {}", user.getUsername());
        } else {
            throw new RuntimeException("User not found with ID: " + userId);
        }
    }


    @Override
    public void updateUser(Long id, User updatedUser) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validation (important for edit)
        String validationError = validateUserData(updatedUser, id);
        if (validationError != null) {
            throw new RuntimeException(validationError);
        }

        existingUser.setName(updatedUser.getName());
        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setContactNumber(updatedUser.getContactNumber());
        existingUser.setRole(updatedUser.getRole());
        existingUser.setActive(updatedUser.isActive());

        userRepository.save(existingUser);
    }


    @Override
    public void rejectUser(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Only delete regular users, not admins
            if (user.getRole() == Role.USER) {
                userRepository.delete(user);
                logger.info("User rejected and deleted: {}", user.getUsername());
            } else {
                // For admin users, just deactivate instead of deleting
                user.setActive(false);
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
                logger.info("Admin user deactivated: {}", user.getUsername());
            }
        } else {
            throw new RuntimeException("User not found with ID: " + userId);
        }
    }

    @Override
    public void toggleUserStatus(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setActive(!user.isActive());
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            logger.info("User status toggled: {} -> {}", user.getUsername(), user.isActive() ? "Active" : "Inactive");
        } else {
            throw new RuntimeException("User not found with ID: " + userId);
        }
    }

    @Override
    public User findByUsername(String name) {
        return userRepository.findByUsername(name).orElse(null);
    }

    // Additional helper method (optional)
    public List<User> getActiveUsers() {
        return userRepository.findByActiveTrue();
    }
}