package com.database.booktrace.repository;

import com.database.booktrace.entity.ReadingLog;
import com.database.booktrace.util.DatabaseConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ReadingLogRepository {
    
    @Autowired
    private DatabaseConnection databaseConnection;

    public List<ReadingLog> findByUserId(Long userId) {
        String sql = "SELECT rl.log_id, rl.user_id, rl.book_id, rl.borrow_date, rl.return_date, " +
                    "rl.mileage, rl.total_mileage, rl.created_at, rl.updated_at, " +
                    "b.title as book_title " +
                    "FROM reading_log rl " +
                    "JOIN books b ON rl.book_id = b.book_id " +
                    "WHERE rl.user_id = ? " +
                    "ORDER BY rl.borrow_date DESC";

        List<ReadingLog> logs = new ArrayList<>();

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ReadingLog log = new ReadingLog();
                log.setLogId(rs.getLong("log_id"));
                log.setBorrowDate(rs.getDate("borrow_date").toLocalDate());
                log.setReturnDate(rs.getDate("return_date") != null ? 
                    rs.getDate("return_date").toLocalDate() : null);
                log.setMileage(rs.getInt("mileage"));
                log.setTotalMileage(rs.getInt("total_mileage"));
                log.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                log.setUpdatedAt(rs.getTimestamp("updated_at") != null ? 
                    rs.getTimestamp("updated_at").toLocalDateTime() : null);
                
                // Book 정보 설정
                log.getBook().setBookId(rs.getLong("book_id"));
                log.getBook().setTitle(rs.getString("book_title"));
                
                logs.add(log);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding reading logs by user ID", e);
        }

        return logs;
    }

} 