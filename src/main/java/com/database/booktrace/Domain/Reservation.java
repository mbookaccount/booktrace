package com.database.booktrace.Domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Getter
@Setter
@ToString
public class Reservation {
    private Long resvId;           // RESV_ID (PK)
    private Long userId;           // USER_ID (FK)
    private Long bookId;           // BOOK_ID (FK)
    private LocalDateTime resvDate;   // RESV_DATE
    private String status;         // STATUS ('ACTIVE', 'COMPLETED', 'CANCELLED')
    private LocalDateTime createdAt;  // CREATED_AT
    private LocalDateTime updatedAt;  // UPDATED_AT

    // 조인 조회 시 추가 정보 (DB 컬럼 아님)
    private String userName;       // 사용자명
    private String bookTitle;      // 도서명
    private String bookAuthor;     // 저자명

    public Reservation() {
        this.status = "ACTIVE";
        this.resvDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}