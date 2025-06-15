package com.database.booktrace.Dto.Response;

import com.database.booktrace.Domain.BookCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendedBookDTO {
    private Long bookId;
    private String title;
    private String author;
    private String publisher;
    private BookCategory category;
    private String coverImage;
    private String description;
    private Integer availableAmount; //가능한 대출 수
    private Boolean isAvailable;
    private Long reservationCount;
}