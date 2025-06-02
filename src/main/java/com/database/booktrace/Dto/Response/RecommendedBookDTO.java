package com.database.booktrace.Dto.Response;

import com.database.booktrace.Domain.BookCategory;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    private Integer reservationCount; //예약 수
}