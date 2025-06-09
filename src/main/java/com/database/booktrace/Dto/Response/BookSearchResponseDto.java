package com.database.booktrace.Dto.Response;


import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookSearchResponseDto {

    private List<BookDetailDto> books;  // 검색된 도서 목록
    private PageInfo pageInfo;          // 페이징 정보
    private SearchInfo searchInfo;      // 검색 정보

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PageInfo {
        private Integer currentPage;     // 현재 페이지 (0부터 시작)
        private Integer pageSize;        // 페이지 크기
        private Long totalElements;      // 전체 데이터 수
        private Integer totalPages;      // 전체 페이지 수
        private Boolean hasNext;         // 다음 페이지 존재 여부
        private Boolean hasPrevious;     // 이전 페이지 존재 여부

        // 전체 페이지 수 계산
        public Integer getTotalPages() {
            if (totalElements == null || pageSize == null || pageSize == 0) {
                return 0;
            }
            return (int) Math.ceil((double) totalElements / pageSize);
        }

        // 다음 페이지 존재 여부
        public Boolean getHasNext() {
            return currentPage != null && currentPage < getTotalPages() - 1;
        }

        // 이전 페이지 존재 여부
        public Boolean getHasPrevious() {
            return currentPage != null && currentPage > 0;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SearchInfo {
        private String searchKeyword;    // 검색 키워드
        private String searchType;       // 검색 타입 (title, author, category, all)
        private String sortBy;           // 정렬 기준
        private String sortDirection;    // 정렬 방향
        private Boolean availableOnly;   // 대출 가능한 도서만 검색했는지
        private Long searchTime;         // 검색 소요 시간 (밀리초)
    }

    // 성공 응답 생성
    public static BookSearchResponseDto success(List<BookDetailDto> books,
                                                PageInfo pageInfo,
                                                SearchInfo searchInfo) {
        return BookSearchResponseDto.builder()
                .books(books)
                .pageInfo(pageInfo)
                .searchInfo(searchInfo)
                .build();
    }

    // 빈 결과 응답 생성
    public static BookSearchResponseDto empty(SearchInfo searchInfo) {
        PageInfo emptyPageInfo = PageInfo.builder()
                .currentPage(0)
                .pageSize(10)
                .totalElements(0L)
                .totalPages(0)
                .hasNext(false)
                .hasPrevious(false)
                .build();

        return BookSearchResponseDto.builder()
                .books(List.of())
                .pageInfo(emptyPageInfo)
                .searchInfo(searchInfo)
                .build();
    }
}
