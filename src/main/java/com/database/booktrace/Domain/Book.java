package com.database.booktrace.Domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
public class Book {
    private Long bookId;           // BOOK_ID (PK)
    private Long libraryId;        // LIBRARY_ID (FK)
    private String title;          // TITLE
    private String author;         // AUTHOR
    private String publisher;      // PUBLISHER
    private LocalDateTime publishedDate;  // PUBLISHED_DATE
    private BookCategory category; // CATEGORY (ENUM)
    private Integer availableAmount;  // AVAILABLE_AMOUNT
    private String coverImage;     // COVER_IMAGE
    private String callNumber;     // CALL_NUMBER
    private LocalDateTime createdAt;  // CREATED_AT
    private LocalDateTime updatedAt;  // UPDATED_AT

    // 조인 조회 시 추가 정보 (DB 컬럼 아님)
    private String libraryName;    // 도서관명

    public Book() {
        this.availableAmount = 1;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}