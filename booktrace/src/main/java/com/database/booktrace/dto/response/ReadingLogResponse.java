package com.database.booktrace.dto.response;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ReadingLogResponse {
    private Integer userLogNumber;  // 사용자별 누적 번호
    private String bookTitle;       // 도서명
    private LocalDate borrowDate;   // 대출일자
    private LocalDate returnDate;   // 반납일자
    private Integer mileage;        // 마일리지
    private Integer totalMileage;   // 누적 마일리지
}
