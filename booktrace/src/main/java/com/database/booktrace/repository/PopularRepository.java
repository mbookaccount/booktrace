package com.database.booktrace.repository;

import com.database.booktrace.dto.PopularBookDTO;
import com.database.booktrace.util.DatabaseConnection;
import oracle.jdbc.OracleTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PopularRepository {
    
    @Autowired
    private DatabaseConnection databaseConnection;

    public List<PopularBookDTO> findWeeklyPopularBooks() {
        try (Connection conn = databaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall(
                "{ call popular_package.get_weekly_popular_books(?) }"
             )) {

            cs.registerOutParameter(1, OracleTypes.CURSOR);
            cs.execute();

            List<PopularBookDTO> books = new ArrayList<>();
            ResultSet rs = (ResultSet) cs.getObject(1);
            while (rs.next()) {
                PopularBookDTO book = new PopularBookDTO();
                book.setBookId(rs.getLong("book_id"));
                book.setTitle(rs.getString("title"));
                book.setAuthor(rs.getString("author"));
                book.setPublisher(rs.getString("publisher"));
                book.setCoverImage(rs.getString("cover_image"));
                
                books.add(book);
            }
            return books;

        } catch (SQLException e) {
            throw new RuntimeException("주간 인기 도서 조회 중 오류가 발생했습니다.", e);
        }
    }

    public List<PopularBookDTO> findMonthlyPopularBooks() {
        try (Connection conn = databaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall(
                "{ call popular_package.get_monthly_popular_books(?) }"
             )) {

            cs.registerOutParameter(1, OracleTypes.CURSOR);
            cs.execute();

            List<PopularBookDTO> books = new ArrayList<>();
            ResultSet rs = (ResultSet) cs.getObject(1);
            while (rs.next()) {
                PopularBookDTO book = new PopularBookDTO();
                book.setBookId(rs.getLong("book_id"));
                book.setTitle(rs.getString("title"));
                book.setAuthor(rs.getString("author"));
                book.setPublisher(rs.getString("publisher"));
                book.setCoverImage(rs.getString("cover_image"));
                
                books.add(book);
            }
            return books;

        } catch (SQLException e) {
            throw new RuntimeException("월간 인기 도서 조회 중 오류가 발생했습니다.", e);
        }
    }
} 