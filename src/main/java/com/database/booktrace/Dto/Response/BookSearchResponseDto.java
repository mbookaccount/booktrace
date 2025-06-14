package com.database.booktrace.Dto.Response;


import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class BookSearchResponseDto {
    private Long bookId;
    private String title;
    private String author;
    private String publisher;
    private LocalDateTime publishedDate;
    private String coverImage;
    private List<BookHoldingInfo> holdings;

    @Getter
    @AllArgsConstructor
    public static class BookHoldingInfo {
        private String libraryName;
        private String callNumber;
        private boolean isAvailable;
    }
}
