package com.database.booktrace.Repository;

import com.database.booktrace.Dto.Response.PopularBookDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.dialect.OracleTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class PopularRepository {

    private final DataSource dataSource;

    /**
     * 주간 인기 도서 목록을 조회합니다.
     * @return 주간 인기 도서 목록
     */
    public List<PopularBookDTO> findWeeklyPopularBooks() {
        return findPopularBooks("popular_package.get_weekly_popular_books");
    }

    /**
     * 월간 인기 도서 목록을 조회합니다.
     * @return 월간 인기 도서 목록
     */
    public List<PopularBookDTO> findMonthlyPopularBooks() {
        return findPopularBooks("popular_package.get_monthly_popular_books");
    }

    /**
     * 인기 도서 목록을 조회하는 공통 메서드입니다.
     * @param procedureName 호출할 프로시저 이름
     * @return 인기 도서 목록
     */
    private List<PopularBookDTO> findPopularBooks(String procedureName) {
        try (Connection conn = dataSource.getConnection();
             CallableStatement cs = conn.prepareCall(
                "{ call " + procedureName + "(?) }"
             )) {

            cs.registerOutParameter(1, OracleTypes.CURSOR);
            cs.execute();

            List<PopularBookDTO> books = new ArrayList<>();
            ResultSet rs = (ResultSet) cs.getObject(1);
            while (rs.next()) {
                books.add(mapPopularBook(rs));
            }
            return books;

        } catch (SQLException e) {
            log.error("인기 도서 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("인기 도서 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * ResultSet을 PopularBookDTO 객체로 매핑합니다.
     * @param rs ResultSet 객체
     * @return PopularBookDTO 객체
     * @throws SQLException SQL 예외 발생 시
     */
    private PopularBookDTO mapPopularBook(ResultSet rs) throws SQLException {
        PopularBookDTO book = new PopularBookDTO();
        book.setBookId(rs.getLong("book_id"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setPublisher(rs.getString("publisher"));
        book.setCoverImage(rs.getString("cover_image"));
        return book;
    }
} 