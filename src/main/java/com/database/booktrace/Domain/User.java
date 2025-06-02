package com.database.booktrace.Domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "USERS")  // Oracle은 대문자 선호
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SequenceGenerator(
        name = "USER_SEQ_GEN",
        sequenceName = "USER_SEQ",
        initialValue = 1,
        allocationSize = 1
)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "USER_SEQ_GEN")
    @Column(name = "USER_ID")
    private Long id;

    @Column(name = "USER_NAME", nullable = false, unique = true, length = 50)
    private String userName;

    @Column(name = "LOGIN_ID", nullable = false, unique = true, length = 50)
    private String userId;

    @Column(name = "PASSWORD", nullable = false, length = 255)
    private String password;

    // Oracle에서는 별도 테이블로 관리하는 것이 좋음
    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "USER_PREFERRED_CATEGORIES",
            joinColumns = @JoinColumn(name = "USER_ID")
    )
    @Column(name = "CATEGORY")
    @Builder.Default
    private Set<BookCategory> preferredCategories = new HashSet<>();

    @Column(name = "MILEAGE", nullable = false)
    @Builder.Default
    private Long mileage = 0L;

    @Column(name = "IS_ACTIVE", nullable = false, length = 1)
    @Builder.Default
    private String isActive = "Y";

    @OneToMany(mappedBy="user",cascade=CascadeType.ALL,fetch=FetchType.LAZY)
    private List<Loan> loans;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reservation> reservations;

    // 비즈니스 로직
    public void addMileage(Long points) {
        if (points > 0) {
            this.mileage += points;
        }
    }

    public void deactivate() {
        this.isActive = "N";
    }

    public void activate() {
        this.isActive = "Y";
    }

    public Boolean getIsActiveBoolean() {
        return "Y".equals(this.isActive);
    }

    public void setIsActiveBoolean(Boolean active) {
        this.isActive = active ? "Y" : "N";
    }

}