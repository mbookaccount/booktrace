package com.database.booktrace.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExtendLoanResponse {
    private Long id;
    private Long userId;
    private Long bookId;
    private LocalDateTime borrowDate;
    private LocalDateTime returnDate;
    private String status;
    private Integer extensionCount;
}
