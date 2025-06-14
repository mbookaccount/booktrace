package com.database.booktrace.Repository;

import com.database.booktrace.Domain.Book;
import com.database.booktrace.Dto.Response.BookSearchResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.dialect.OracleTypes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Repository
@RequiredArgsConstructor
@Slf4j
public class BookRepository {

    private final DataSource dataSource;

    public BookSearchResponseDto getBookDetailByTitle(String title) {
        List<BookSearchResponseDto.BookHoldingInfo> holdings = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             CallableStatement cs = conn.prepareCall("{ call SEARCH_BOOK_DETAIL(?, ?, ?) }")) {

            cs.setString(1, title); // IN: 제목
            cs.registerOutParameter(2, OracleTypes.CURSOR); // OUT: 대표 책 정보
            cs.registerOutParameter(3, OracleTypes.CURSOR); // OUT: 소장 도서관 목록

            cs.execute();

            // 대표 책 정보
            ResultSet mainBookRs = (ResultSet) cs.getObject(2);
            if (!mainBookRs.next()) return null;

            Long bookId = mainBookRs.getLong("BOOK_ID");
            String bookTitle = mainBookRs.getString("TITLE");
            String author = mainBookRs.getString("AUTHOR");
            String publisher = mainBookRs.getString("PUBLISHER");
            Timestamp pubTimestamp = mainBookRs.getTimestamp("PUBLISHED_DATE");
            LocalDateTime publishedDate = (pubTimestamp != null) ? pubTimestamp.toLocalDateTime() : null;
            String coverImage = mainBookRs.getString("COVER_IMAGE");

            // 소장 도서관 목록
            ResultSet holdingRs = (ResultSet) cs.getObject(3);
            while (holdingRs.next()) {
                String libraryName = holdingRs.getString("LIBRARY_NAME");
                String callNumber = holdingRs.getString("CALL_NUMBER");
                boolean isAvailable = "Y".equals(holdingRs.getString("IS_AVAILABLE"));

                holdings.add(new BookSearchResponseDto.BookHoldingInfo(libraryName, callNumber, isAvailable));
            }

            return new BookSearchResponseDto(
                    bookId, bookTitle, author, publisher,
                    publishedDate, coverImage, holdings
            );

        } catch (Exception e) {
            log.error("책 상세 검색 실패: {}", e.getMessage(), e);
            throw new RuntimeException("책 상세 검색 중 오류 발생");
        }
    }
//    // Oracle용 카테고리별 추천 도서 조회 (subquery, Oracle 함수 사용)
//    @Query(value = """
//    SELECT * FROM (
//        SELECT b.*
//        FROM BOOKS b
//        WHERE b.CATEGORY = ?1
//        AND NOT EXISTS (
//            SELECT 1 FROM LOANS l
//            WHERE l.BOOK_ID = b.BOOK_ID
//            AND l.USER_ID = ?2
//            AND l.STATUS = 'BORROWED'
//        )
//        ORDER BY NVL(b.AVAILABLE_AMOUNT, 0) DESC
//    ) WHERE ROWNUM <= ?3
//    """, nativeQuery = true)
//    List<Book> findRecommendedBooksByCategory(
//            @Param("category") String category,
//            @Param("userId") Long userId,
//            @Param("limit") int limit
//    );
//
//    // Oracle용 인기 도서 조회 (Oracle 함수 사용)
//    @Query(value = """
//            SELECT * FROM (
//                SELECT * FROM BOOKS
//                WHERE NVL(AVAILABLE_AMOUNT, 0) >= 0
//                ORDER BY NVL(AVAILABLE_AMOUNT, 0) DESC, BOOK_ID ASC
//            ) WHERE ROWNUM <= ?1
//            """, nativeQuery = true)
//    List<Book> findPopularBooks(int limit);
//
//    // 도서관별 도서 조회 (네이티브 SQL만 사용)
//    @Query(value = "SELECT * FROM BOOKS WHERE LIBRARY_ID = ?1 ORDER BY TITLE", nativeQuery = true)
//    List<Book> findByLibraryId(@Param("libraryId") Long libraryId);
//
//    // 도서 ID와 도서관 ID로 도서 조회 (대출용, 네이티브 SQL)
//    @Query(value = "SELECT * FROM BOOKS WHERE BOOK_ID = ?1 AND LIBRARY_ID = ?2", nativeQuery = true)
//    Book findByBookIdAndLibraryId(@Param("bookId") Long bookId, @Param("libraryId") Long libraryId);
//
//    // 대출 가능한 도서 조회 (Oracle NVL 함수 사용)
//    @Query(value = """
//        SELECT * FROM BOOKS
//        WHERE NVL(AVAILABLE_AMOUNT, 0) > 0
//        ORDER BY TITLE
//        """, nativeQuery = true)
//    List<Book> findAvailableBooks();
//
//    // 제목 검색 (Oracle UPPER 함수 사용)
//    @Query(value = """
//        SELECT * FROM BOOKS
//        WHERE UPPER(TITLE) LIKE UPPER('%' || ?1 || '%')
//        ORDER BY TITLE
//        """, nativeQuery = true)
//    List<Book> findByTitleContaining(String title);
}