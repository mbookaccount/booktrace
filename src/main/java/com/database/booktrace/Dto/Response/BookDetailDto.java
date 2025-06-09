package com.database.booktrace.Dto.Response;


import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookDetailDto {

    private Long bookId;
    private Long libraryId;
    private String libraryName;        // JOIN으로 가져온 도서관 이름

    private String title;
    private String author;
    private String publisher;
    private LocalDateTime publishedDate;
    private String category;
    private String categoryDisplayName; // 한글 카테고리명

    private Integer availableAmount;
    private Boolean isAvailable;       // 대출 가능 여부
    private String coverImage;
    private String callNumber;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 추가 정보
    private Integer totalAmount;       // 전체 보유량 (대출중 + 보유중)
    private Integer borrowedAmount;    // 현재 대출중인 수량
    private Integer reservationCount;  // 예약 대기자 수

    // 대출 가능 여부 계산
    public Boolean getIsAvailable() {
        return availableAmount != null && availableAmount > 0;
    }

    // 카테고리 한글명 매핑
    public String getCategoryDisplayName() {
        if (category == null) return "";

        return switch (category) {
            case "HEALTH" -> "건강";
            case "ECONOMY" -> "경제";
            case "SCIENCE" -> "과학";
            case "TECHNOLOGY" -> "기술";
            case "LITERATURE" -> "문학";
            case "NOVEL" -> "소설";
            case "HISTORY" -> "역사";
            case "SELF_DEVELOPMENT" -> "자기계발";
            default -> category;
        };
    }
}