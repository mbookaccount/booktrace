package com.database.booktrace.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PopularBookDTO {
    private Long bookId;
    private String title;
    private String coverColor;  // 카테고리별 색상
} 