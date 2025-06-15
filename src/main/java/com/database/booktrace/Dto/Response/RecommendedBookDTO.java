package com.database.booktrace.Dto.Response;

import com.database.booktrace.Domain.BookCategory;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendedBookDTO {
    private Long bookId;
    private String title;
    private BookCategory category;
    private String coverImage;
}