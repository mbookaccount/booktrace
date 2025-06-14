package com.database.booktrace.Domain;
import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@ToString
public class User {
    private Long userId;           // USER_ID (PK)
    private String userName;       // USER_NAME
    private String loginId;        // LOGIN_ID (UNIQUE)
    private String password;       // PASSWORD
    private Integer mileage;       // MILEAGE
    private Character isActive;    // IS_ACTIVE ('Y' or 'N')
    private LocalDateTime createdAt;  // CREATED_AT
    private LocalDateTime updatedAt;  // UPDATED_AT

    // 추가 필드 (조인 조회 시 사용, DB 컬럼 아님)
    private Set<BookCategory> preferredCategories;

    public User() {
        this.mileage = 0;
        this.isActive = 'Y';
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}