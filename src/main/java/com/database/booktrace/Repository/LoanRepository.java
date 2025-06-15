package com.database.booktrace.Repository;

import com.database.booktrace.Domain.Loan;
import com.database.booktrace.Dto.Request.LoanRequestDTO;

import com.database.booktrace.Dto.Response.CancelResvResponse;
import com.database.booktrace.Dto.Response.ExtendLoanResponse;
import com.database.booktrace.Dto.Response.LoanResponse;
import com.database.booktrace.Dto.Response.LoanResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.dialect.OracleTypes;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
@Slf4j
public class LoanRepository {

//    //사용자가 대출한 도서id 목록 조회
//    @Query(value="SELECT l.BOOK_ID FROM LOANS l WHERE l.USER_ID=:userId  AND l.status = 'BORROWED'",nativeQuery = true)
//    Set<Long> findBorrowedBookIdsByUserId(@Param("userId") Long id);

    public final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    public Map<String, Object> advancedBorrowEbook(Long userId, Long bookId) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("loan_package.BORROW_BOOK")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_user_id", Types.NUMERIC),
                        new SqlParameter("p_book_id", Types.NUMERIC),
                        new SqlOutParameter("p_result", Types.INTEGER),
                        new SqlOutParameter("p_message", Types.VARCHAR),
                        new SqlOutParameter("p_loan_id", Types.NUMERIC)
                );

        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_user_id", userId);
        inParams.put("p_book_id", bookId);

        return jdbcCall.execute(inParams);
    }
//
//    public LoanResponseDTO borrowBookUsingProcedure(LoanRequestDTO request) {
//        return jdbcTemplate.execute((Connection connection) -> {
//            // Callable Statement 생성
//            String sql = "{call BORROW_BOOK(?, ?, ?, ?, ?)}";
//
//            try (CallableStatement callableStatement = connection.prepareCall(sql)) {
//                // IN 매개변수 설정
//                callableStatement.setLong(1, request.getUserId());
//                callableStatement.setLong(2, request.getBookId());
//                callableStatement.setLong(3, request.getLibraryId());
//
//                // OUT 매개변수 등록
//                callableStatement.registerOutParameter(4, Types.NUMERIC); // p_loan_id
//                callableStatement.registerOutParameter(5, Types.VARCHAR); // p_result
//
//                // 프로시저 실행
//                callableStatement.execute();
//
//                // 결과 가져오기
//                Long loanId = callableStatement.getLong(4);
//                String result = callableStatement.getString(5);
//
//                log.info("PL/SQL 프로시저 실행 결과: loanId={}, result={}", loanId, result);
//
//                // 결과 분석해서 DTO 반환
//                if (result.startsWith("SUCCESS")) {
//                    return LoanResponseDTO.success(loanId, result);
//                } else {
//                    return LoanResponseDTO.failure(result);
//                }
//
//            } catch (SQLException e) {
//                log.error("PL/SQL 프로시저 호출 중 오류 발생", e);
//                return LoanResponseDTO.failure("시스템 오류: " + e.getMessage());
//            }
//        });
//
//
//    }
//    public boolean checkBookAvailability(Long bookId, Long libraryId) {
//            String sql = """
//            SELECT COUNT(*)
//            FROM BOOKS
//            WHERE BOOK_ID = ?
//            AND LIBRARY_ID = ?
//            AND NVL(AVAILABLE_AMOUNT, 0) > 0
//            """;
//
//            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, bookId, libraryId);
//            return count != null && count > 0;
//        }

// =============================================

    private LoanResponse mapLoanResponse(ResultSet rs) throws SQLException {
        LoanResponse response = new LoanResponse();
        response.setNo(rs.getInt("no"));
        response.setLoanId(rs.getLong("loan_id"));
        response.setLoanDate(rs.getTimestamp("loan_date").toLocalDateTime().toLocalDate());
        response.setDueDate(rs.getTimestamp("due_date").toLocalDateTime().toLocalDate());
        response.setBookTitle(rs.getString("book_title"));
        response.setLibraryLocation(rs.getString("library_location"));
        response.setStatus(rs.getString("status"));
        response.setIsExtendable(rs.getBoolean("is_extendable"));
        response.setAction(rs.getString("action"));
        return response;
    };

    // 사용자의 대출 목록 조회
    public List<LoanResponse> findByUserId(Long userId) {
        try (Connection conn = dataSource.getConnection();
             CallableStatement cs = conn.prepareCall(
                     "{ call loan_package.get_user_loans(?, ?) }"
             )) {

            cs.setLong(1, userId);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();

            List<LoanResponse> loans = new ArrayList<>();
            ResultSet rs = (ResultSet) cs.getObject(2);
            while (rs.next()) {
                loans.add(mapLoanResponse(rs));
            }
            return loans;

        } catch (SQLException e) {
            handleSQLException(e, "사용자의 대출 목록을 조회하는 중 오류가 발생했습니다.");
            return new ArrayList<>();
        }
    }

    // 대출 연장
    public ExtendLoanResponse extendLoan(Long loanId) {
        try (Connection conn = dataSource.getConnection();
             CallableStatement cs = conn.prepareCall(
                     "{ call loan_package.extend_loan(?, ?) }"
             )) {

            cs.setLong(1, loanId);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();

            ResultSet rs = (ResultSet) cs.getObject(2);
            if (rs.next()) {
                ExtendLoanResponse response = new ExtendLoanResponse();
                response.setId(rs.getLong("id"));
                response.setUserId(rs.getLong("user_id"));
                response.setBookId(rs.getLong("book_id"));
                response.setBorrowDate(rs.getTimestamp("borrow_date").toLocalDateTime());
                response.setReturnDate(rs.getTimestamp("return_date").toLocalDateTime());
                response.setStatus(rs.getString("status"));
                response.setExtensionCount(rs.getInt("extensionCount"));
                return response;
            }

            throw new IllegalArgumentException("대출 연장이 불가능합니다.");

        } catch (SQLException e) {
            handleSQLException(e, "대출 연장 중 오류가 발생했습니다.");
            return null;
        }
    }

    // 예약 취소
    public CancelResvResponse cancelReservation(Long reservationId) {
        try (Connection conn = dataSource.getConnection();
             CallableStatement cs = conn.prepareCall(
                     "{ call loan_package.cancel_reservation(?) }"
             )) {

            cs.setLong(1, reservationId);
            cs.execute();

            ResultSet rs = (ResultSet) cs.getObject(2);
            if (rs.next()) {
                CancelResvResponse response = new CancelResvResponse();
                response.setId(rs.getLong("id"));
                response.setUserId(rs.getLong("user_id"));
                response.setBookId(rs.getLong("book_id"));
                response.setResvDate(rs.getTimestamp("resv_date").toLocalDateTime());
                response.setStatus(rs.getString("status"));
                return response;
            }

            throw new IllegalArgumentException("예약 취소가 불가능합니다.");
        } catch (SQLException e) {
            handleSQLException(e, "예약 취소 중 오류가 발생했습니다.");
            return null;
        }
    }

    // 도서 예약 여부 확인
    public boolean hasReservation(Long bookId) {
        try (Connection conn = dataSource.getConnection();
             CallableStatement cs = conn.prepareCall(
                     "{ ? = call loan_package.has_reservation(?) }"
             )) {

            cs.registerOutParameter(1, Types.NUMERIC);
            cs.setLong(2, bookId);
            cs.execute();

            return cs.getInt(1) == 1;

        } catch (SQLException e) {
            handleSQLException(e, "예약 여부 확인 중 오류가 발생했습니다.");
            return false;
        }
    }

    // 대출 정보 삭제
    public void deleteLoan(Long loanId) {
        try (Connection conn = dataSource.getConnection();
             CallableStatement cs = conn.prepareCall(
                     "{ call loan_package.delete_loan(?) }"
             )) {

            cs.setLong(1, loanId);
            cs.execute();

        } catch (SQLException e) {
            handleSQLException(e, "대출 정보 삭제 중 오류가 발생했습니다.");
        }
    }

    // 대출 연장 가능 여부 확인
    public boolean canExtendLoan(Long loanId) {
        try (Connection conn = dataSource.getConnection();
             CallableStatement cs = conn.prepareCall(
                     "{ ? = call loan_package.can_extend_loan(?) }"
             )) {

            cs.registerOutParameter(1, Types.NUMERIC);
            cs.setLong(2, loanId);
            cs.execute();

            return cs.getInt(1) == 1;

        } catch (SQLException e) {
            handleSQLException(e, "대출 연장 가능 여부 확인 중 오류가 발생했습니다.");
            return false;
        }
    }

    private void handleSQLException(SQLException e, String message) {
        log.error("SQL 오류 발생: {}", e.getMessage(), e);
        switch (e.getErrorCode()) {
            case 20001: // 대출 불가
                throw new IllegalArgumentException("대출이 불가능한 도서입니다.");
            case 20002: // 연체
                throw new IllegalArgumentException("연체된 도서가 있어 대출이 불가능합니다.");
            case 20003: // 대출 한도 초과
                throw new IllegalArgumentException("대출 한도를 초과했습니다.");
            case 20004: // 예약 불가
                throw new IllegalArgumentException("예약이 불가능한 도서입니다.");
            case 20005: // 연장 불가
                throw new IllegalArgumentException("대출 연장이 불가능합니다.");
            default:
                throw new RuntimeException(message, e);
        }
    }

}
