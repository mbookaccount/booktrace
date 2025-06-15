package com.database.booktrace.Dto.Response;

import lombok.Data;

@Data
public class PopularBookDTO {
    private Long bookId;
    private String title;
    //    private String author;
//    private String publisher;
    private String coverImage;  // 이미지 url
} 