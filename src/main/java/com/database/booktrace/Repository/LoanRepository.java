package com.database.booktrace.Repository;

import com.database.booktrace.Dto.Request.LoanRequestDTO;

import com.database.booktrace.Dto.Response.LoanResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

@Repository
@RequiredArgsConstructor
@Slf4j
public class LoanRepository {

//    //사용자가 대출한 도서id 목록 조회
//    @Query(value="SELECT l.BOOK_ID FROM LOANS l WHERE l.USER_ID=:userId  AND l.status = 'BORROWED'",nativeQuery = true)
//    Set<Long> findBorrowedBookIdsByUserId(@Param("userId") Long id);

    public final JdbcTemplate jdbcTemplate;

    public LoanResponseDTO borrowBookUsingProcedure(LoanRequestDTO request) {
        return jdbcTemplate.execute((Connection connection) -> {
            // Callable Statement 생성
            String sql = "{call BORROW_BOOK(?, ?, ?, ?, ?)}";

            try (CallableStatement callableStatement = connection.prepareCall(sql)) {
                // IN 매개변수 설정
                callableStatement.setLong(1, request.getUserId());
                callableStatement.setLong(2, request.getBookId());
                callableStatement.setLong(3, request.getLibraryId());

                // OUT 매개변수 등록
                callableStatement.registerOutParameter(4, Types.NUMERIC); // p_loan_id
                callableStatement.registerOutParameter(5, Types.VARCHAR); // p_result

                // 프로시저 실행
                callableStatement.execute();

                // 결과 가져오기
                Long loanId = callableStatement.getLong(4);
                String result = callableStatement.getString(5);

                log.info("PL/SQL 프로시저 실행 결과: loanId={}, result={}", loanId, result);

                // 결과 분석해서 DTO 반환
                if (result.startsWith("SUCCESS")) {
                    return LoanResponseDTO.success(loanId, result);
                } else {
                    return LoanResponseDTO.failure(result);
                }

            } catch (SQLException e) {
                log.error("PL/SQL 프로시저 호출 중 오류 발생", e);
                return LoanResponseDTO.failure("시스템 오류: " + e.getMessage());
            }
        });


    }
    public boolean checkBookAvailability(Long bookId, Long libraryId) {
            String sql = """
            SELECT COUNT(*)
            FROM BOOKS
            WHERE BOOK_ID = ?
            AND LIBRARY_ID = ?
            AND NVL(AVAILABLE_AMOUNT, 0) > 0
            """;

            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, bookId, libraryId);
            return count != null && count > 0;
        }


}
