package com.database.booktrace.repository;

import com.database.booktrace.dto.PopularBookDTO;
import com.database.booktrace.util.DatabaseConnection;
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
        String sql = "SELECT * FROM weekly_popular_books FETCH FIRST 10 ROWS ONLY";
        return executePopularBooksQuery(sql);
    }

    public List<PopularBookDTO> findMonthlyPopularBooks() {
        String sql = "SELECT * FROM monthly_popular_books FETCH FIRST 10 ROWS ONLY";
        return executePopularBooksQuery(sql);
    }

    private List<PopularBookDTO> executePopularBooksQuery(String sql) {
        List<PopularBookDTO> books = new ArrayList<>();

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                PopularBookDTO book = new PopularBookDTO();
                book.setBookId(rs.getLong("book_id"));
                book.setTitle(rs.getString("title"));
                book.setCoverColor(rs.getString("cover_color"));
                
                books.add(book);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding popular books", e);
        }

        return books;
    }
} 