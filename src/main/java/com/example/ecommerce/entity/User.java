package com.example.ecommerce.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="USERS")
public class User {

    @Id
    @SequenceGenerator(name="userSeq", sequenceName="USERS_SEQ", allocationSize=1)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="userSeq")
    @Column(name="USER_ID", nullable=false)
    private Long userId;

    @Column(name="FULL_NAME", nullable=true, length=100)
    private String fullName;

    @Column(name="EMAIL", nullable=true, length=100)
    private String email;

    @Column(name="PHONE", nullable=true, length=15)
    private String phone;

    @Column(name="USERNAME", nullable=true, length=50)
    private String username;

    @Column(name="PASSWORD_HASH", nullable=true, length=100)
    private String passwordHash;

    @Column(name="CREATED_AT", nullable=true, insertable=false, updatable=false)
    private LocalDateTime createdAt;

    public User() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

}