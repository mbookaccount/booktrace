package com.database.booktrace.Domain;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class Loan {
    private Long logId;            // LOG_ID (PK)
    private Long userId;           // USER_ID (FK)
    private Long bookId;           // BOOK_ID (FK)
    private LocalDateTime borrowDate;  // BORROW_DATE
    private LocalDateTime returnDate;  // RETURN_DATE
    private Integer extendNumber;  // EXTEND_NUMBER
    private String status;         // STATUS ('BORROWED', 'RETURNED')
    private LocalDateTime createdAt;  // CREATED_AT
    private LocalDateTime updatedAt;  // UPDATED_AT

    // 조인 조회 시 추가 정보 (DB 컬럼 아님)
    private String userName;       // 사용자명
    private String bookTitle;      // 도서명
    private String bookAuthor;     // 저자명
    private String libraryName;    // 도서관명

    public Loan() {
        this.extendNumber = 0;
        this.status = "BORROWED";
        this.borrowDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}