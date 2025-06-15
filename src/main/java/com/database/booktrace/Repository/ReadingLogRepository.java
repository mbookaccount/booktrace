package com.database.booktrace.Repository;

import com.database.booktrace.Dto.Response.ReadingLogResponse;
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
public class ReadingLogRepository {

    private final DataSource dataSource;

    /**
     * 사용자의 독서 로그 목록을 조회합니다.
     * @param userId 사용자 ID
     * @return 독서 로그 목록
     */
    public List<ReadingLogResponse> findByUserId(Long userId) {
        try (Connection conn = dataSource.getConnection();
             CallableStatement cs = conn.prepareCall(
                     "{ call reading_log_package.get_user_reading_logs(?, ?) }"
             )) {

            cs.setLong(1, userId);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();

            List<ReadingLogResponse> logs = new ArrayList<>();
            ResultSet rs = (ResultSet) cs.getObject(2);

            while (rs.next()) {
                ReadingLogResponse response = new ReadingLogResponse();
                response.setUserLogNumber(rs.getInt("user_log_number"));
                response.setBookTitle(rs.getString("book_title"));
                response.setMileage(rs.getInt("mileage"));
                response.setTotalMileage(rs.getInt("total_mileage"));
                response.setBorrowDate(rs.getTimestamp("borrow_date").toLocalDateTime().toLocalDate());
                response.setReturnDate(rs.getTimestamp("return_date") != null ? 
                    rs.getTimestamp("return_date").toLocalDateTime().toLocalDate() : null);
                logs.add(response);
            }

            return logs;

        } catch (SQLException e) {
            handleSQLException(e, "사용자의 독서 로그를 조회하는 중 오류가 발생했습니다.");
            throw new RuntimeException("독서 로그 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * SQL 예외를 처리합니다.
     * @param e SQL 예외
     * @param message 기본 오류 메시지
     */
    private void handleSQLException(SQLException e, String message) {
        log.error("SQL 오류 발생: {}", e.getMessage(), e);
        switch (e.getErrorCode()) {
            case 30001: // 독서 로그 없음
                throw new IllegalArgumentException("독서 로그를 찾을 수 없습니다.");
            case 30002: // 날짜 유효성 오류
                throw new IllegalArgumentException("반납일은 대출일보다 이후여야 합니다.");
            case 30003: // 마일리지 계산 오류
                throw new IllegalArgumentException("마일리지 계산 중 오류가 발생했습니다.");
            default:
                throw new RuntimeException(message, e);
        }
    }
} 