package com.database.booktrace.Dto.Request;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookSearchRequestDto {

    // 검색 조건들
    private String title;           // 제목 검색
    private String author;          // 저자 검색
    private String category;        // 카테고리 검색
    private Long libraryId;         // 도서관 ID
    private String keyword;         // 통합 검색 (제목 + 저자)
    private Boolean availableOnly;  // 대출 가능한 도서만

    // 페이징 조건
    private Integer page;           // 페이지 번호 (0부터 시작)
    private Integer size;           // 페이지 크기
    private String sortBy;          // 정렬 기준 (title, author, publishedDate)
    private String sortDirection;   // 정렬 방향 (asc, desc)

    // 기본값 설정
    public Integer getPage() {
        return page != null ? page : 0;
    }

    public Integer getSize() {
        return size != null ? size : 10;
    }

    public String getSortBy() {
        return sortBy != null ? sortBy : "title";
    }

    public String getSortDirection() {
        return sortDirection != null ? sortDirection : "asc";
    }

    public Boolean getAvailableOnly() {
        return availableOnly != null ? availableOnly : false;
    }
}