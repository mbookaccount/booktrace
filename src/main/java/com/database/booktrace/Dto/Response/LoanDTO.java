package com.database.booktrace.Dto.Response;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class LoanDTO {
    private Long loanId;           // 대출 ID
    private Long userId;           // 사용자 ID
    private Long bookId;           // 도서 ID
    private String bookTitle;      // 도서명
    private LocalDate borrowDate;   // 대출일자
    private LocalDate dueDate;      // 반납예정일
    private LocalDate returnDate;   // 반납일자
    private String status;         // 대출상태 (BORROWED, RETURNED, OVERDUE)
    private LocalDate createdAt;    // 생성일시
    private LocalDate updatedAt;    // 수정일시
} 