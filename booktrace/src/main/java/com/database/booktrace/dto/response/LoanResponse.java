package com.database.booktrace.dto.response;

import lombok.Data;
import java.time.LocalDate;

@Data
public class LoanResponse {
    private Integer no;              // 사용자별 대출목록 번호
    private Long loanId;            // 대출 ID
    private LocalDate loanDate;     // 대출일
    private LocalDate dueDate;      // 반납예정일
    private String bookTitle;       // 도서명
    private String libraryLocation; // 도서관 위치
    private String status;          // 대출상태
    private Boolean isExtendable;   // 연장가능여부
    private String action;          // 가능한 액션
} 