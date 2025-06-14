package com.database.booktrace.Dto.Response;

import lombok.Data;

@Data
public class PopularBookDTO {
    private Long bookId;
    private String title;
    private String author;
    private String publisher;
    private String coverImage;  // 카테고리별 색상
} 