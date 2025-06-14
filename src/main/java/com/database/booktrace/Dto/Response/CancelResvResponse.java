package com.database.booktrace.Dto.Response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CancelResvResponse {
    private Long id;
    private Long userId;
    private Long bookId;
    private LocalDateTime resvDate;
    private String status;
    private Integer extensionCount;
}
