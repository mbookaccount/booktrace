package com.database.booktrace.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class ReadingLogDTO {
    private Long logId;            // 독서 로그 ID
    private Long userId;           // 사용자 ID
    private Long bookId;           // 도서 ID
    private String bookTitle;      // 도서명
    private LocalDate borrowDate;   // 대출일자
    private LocalDate returnDate;   // 반납일자
    private Integer mileage;        // 마일리지
    private Integer totalMileage;   // 누적 마일리지
    private LocalDate createdAt;    // 생성일시
    private LocalDate updatedAt;    // 수정일시
} 