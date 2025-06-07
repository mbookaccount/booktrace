package com.database.booktrace.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "reading_log")
@Getter
@Setter
public class ReadingLog {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reading_log_seq")
    @SequenceGenerator(name = "reading_log_seq", sequenceName = "reading_log_seq", allocationSize = 1)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "book_id")
    private Long bookId;

    @Column(name = "borrow_date")
    private LocalDateTime borrowDate;

    @Column(name = "return_date")
    private LocalDateTime returnDate;

    @Column(name = "mileage")
    private Integer mileage;

    @Column(name = "total_mileage")
    private Integer totalMileage;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Transient
    private String bookTitle;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 