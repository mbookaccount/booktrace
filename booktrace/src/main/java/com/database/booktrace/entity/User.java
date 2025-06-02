package com.database.booktrace.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", sequenceName = "user_seq", allocationSize = 1)
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name="user_name", nullable = false)
    private String userName;

    @Column(name="login_id", nullable = false)
    private String LoginId;

    @Column(nullable = false)
    private String password;

    @Column
    private Integer mileage = 0;

    @Column(columnDefinition = "VARCHAR2(4000)")
    private String interestsJson;

    @Transient
    private Set<String> interests = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

} 