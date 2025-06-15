package com.database.booktrace.Repository;

import com.database.booktrace.Dto.Response.RecommendedBookDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
@RequiredArgsConstructor
public class RecommendationRepository {

    private final DataSource dataSource;

    public List<RecommendedBookDTO> getRecommendedBooks(Long userId, int limit) {
        List<RecommendedBookDTO> recommendedBooks = new ArrayList<>();

        String call = "{ call recommendation_pkg.get_recommended_books(?, ?, ?) }";

        try (Connection conn = dataSource.getConnection();
             CallableStatement cs = conn.prepareCall(call)) {

            cs.setLong(1, userId);
            cs.setInt(2, limit);
            cs.registerOutParameter(3, Types.REF_CURSOR);

            cs.execute();

            try (ResultSet rs = (ResultSet) cs.getObject(3)) {
                while (rs.next()) {
                    RecommendedBookDTO book = new RecommendedBookDTO();
                    book.setBookId(rs.getLong("book_id"));
                    book.setTitle(rs.getString("title"));
                    book.setAuthor(rs.getString("author"));
                    book.setPublisher(rs.getString("publisher"));
                    book.setCoverImage(rs.getString("cover_image"));
                    book.setDescription(rs.getString("description"));
                    book.setAvailableAmount(rs.getInt("available_amount"));
                    book.setIsAvailable(rs.getInt("is_available") == 1);

                    recommendedBooks.add(book);
                }
            }

        } catch (SQLException e) {
            log.error("추천 도서 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("추천 도서 조회 중 오류 발생", e);
        }

        return recommendedBooks;
    }
} 