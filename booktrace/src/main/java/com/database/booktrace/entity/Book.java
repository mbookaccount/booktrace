package com.database.booktrace.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "books")
@Getter
@Setter
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "book_seq")
    @SequenceGenerator(name = "book_seq", sequenceName = "book_seq", allocationSize = 1)
    @Column(name = "book_id")
    private Long bookId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Library library;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private String publisher;

    @Column(name = "published_date")
    private LocalDate publishedDate;

    @Column
    private String category;

    @Column(name = "available_amount")
    private Integer availableAmount = 0;

    @Column(name = "borrow_count")
    private Integer borrowCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "cover_image", nullable = false)
    private String coverImage;

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