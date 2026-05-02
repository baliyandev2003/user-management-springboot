package com.example.baliyan.repository;

import com.example.baliyan.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByContactNumber(String contactNumber);
    Optional<User> findByResetToken(String resetToken);


    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByContactNumber(String contactNumber);

    // For edit operations - check if exists for other users
    boolean existsByUsernameAndIdNot(String username, Long id);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByContactNumberAndIdNot(String contactNumber, Long id);

    @Query("SELECT u FROM User u WHERE u.role = com.example.baliyan.Model.Role.USER AND u.approved = false")
    List<User> findPendingApprovals();

    List<User> findByApprovedFalse();
    List<User> findByApprovedTrue();
    List<User> findByActiveTrue();
}