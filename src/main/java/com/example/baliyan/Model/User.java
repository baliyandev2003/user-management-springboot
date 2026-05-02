package com.example.baliyan.Model;

import jakarta.persistence.*;
import javax.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Min(value = 1, message = "Age must be at least 1")
    @Max(value = 150, message = "Age must be less than 150")
    private Integer age;

    @Pattern(regexp = "^[0-9]{10}$", message = "Contact number must be 10 digits")
    @Column(name = "contact_number", unique = true)
    private String contactNumber;



//    @Lob
//    @Column(name = "profile_image", columnDefinition = "BYTEA")
//    private byte[] profileImage;
//    private String profilePhoto;


    @NotNull(message = "Role is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "address")
    private String address;

    @Column(name = "bio", length = 500)
    private String bio;

    @Column(name = "active")
    private boolean active = true;

    @Column(name = "approved")
    private boolean approved = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "token_expiry")
    private LocalDateTime tokenExpiry;







    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    private String profilePhoto;

    // Constructors
    public User() {}

    public User(String username, String password, String email, String name, Role role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.name = name;
        this.role = role;
        this.approved = (role == Role.ADMIN );
    }

    // Getters and Setters

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getProfilePhoto() { return profilePhoto; }
    public void setProfilePhoto(String profilePhoto) { this.profilePhoto = profilePhoto; }

    public String getResetToken() {
        return resetToken;
    }

//    public void setResetToken(String resetToken) {
//        this.resetToken = resetToken;
//    }

    public LocalDateTime getTokenExpiry() {
        return tokenExpiry;
    }

    public void setTokenExpiry(LocalDateTime tokenExpiry) {
        this.tokenExpiry = tokenExpiry;
    }


    public Role getRole() { return role; }
    public void setRole(Role role) {
        this.role = role;
        if (role == Role.ADMIN ) {
            this.approved = true;
        }
    }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }



    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}