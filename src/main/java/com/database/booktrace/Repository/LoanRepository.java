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

    public final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    public Map<String, Object> BorrowEbook(Long userId, Long bookId) {
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

    //도서 중도 반납하기
    public Map<String,Object> returnBook(Long loanId){
        log.info("반납 요청 - 대출id {}",loanId);

        SimpleJdbcCall jdbcCall=new SimpleJdbcCall(dataSource)
                .withProcedureName("return_package.return_book")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_loan_id",Types.NUMERIC),
                        new SqlOutParameter("p_result",Types.INTEGER),
                        new SqlOutParameter("p_message",Types.VARCHAR)
                );

    Map<String,Object> inParams=new HashMap<>();
    inParams.put("p_loan_id",loanId);

    Map<String,Object> result=jdbcCall.execute(inParams);

    log.info("반납 결과 - 결과 {} 메시지 {}",result.get("p_result"),result.get("p_message"));

    return result;
    }

    private LoanResponse mapLoanResponse(ResultSet rs) throws SQLException {
        LoanResponse response = new LoanResponse();

        response.setNo(rs.getInt("no"));
        response.setLoanId(rs.getLong("loan_id"));

        // loan_date
        Timestamp loanDate = rs.getTimestamp("loan_date");
        if (loanDate != null) {
            response.setLoanDate(loanDate.toLocalDateTime().toLocalDate());
        }

        // due_date
        Timestamp dueDate = rs.getTimestamp("due_date");
        if (dueDate != null) {
            response.setDueDate(dueDate.toLocalDateTime().toLocalDate());
        } else {
            response.setDueDate(null); // 또는 LocalDate.now()
        }

        response.setBookTitle(rs.getString("book_title"));
        response.setLibraryLocation(rs.getString("library_location"));
        response.setStatus(rs.getString("status"));
        response.setIsExtendable(rs.getBoolean("is_extendable"));
        response.setAction(rs.getString("action"));

        return response;
    }


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
                //response.setBorrowDate(rs.getTimestamp("borrow_date").toLocalDateTime());
                //response.setReturnDate(rs.getTimestamp("return_date").toLocalDateTime());
                Timestamp borrowTs = rs.getTimestamp("borrow_date");
                response.setBorrowDate(borrowTs != null ? borrowTs.toLocalDateTime() : null);

                Timestamp returnTs = rs.getTimestamp("return_date");
                response.setReturnDate(returnTs != null ? returnTs.toLocalDateTime() : null);
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



    // Repository
    public CancelResvResponse cancelReservation(Long reservationId) {
        System.out.println("=== 예약 취소 시도 ===");
        System.out.println("전달받은 reservationId: " + reservationId);

        try (Connection conn = dataSource.getConnection();
             CallableStatement cs = conn.prepareCall(
                     "{ call loan_package.cancel_reservation(?, ?) }"
             )) {

            cs.setLong(1, reservationId);
            cs.registerOutParameter(2, OracleTypes.CURSOR);

            System.out.println("PL/SQL 호출 전...");
            cs.execute();
            System.out.println("PL/SQL 호출 성공!");
//            cs.registerOutParameter(2, OracleTypes.CURSOR);
//            cs.execute();

            try (ResultSet rs = (ResultSet) cs.getObject(2)) { // try-with-resources로 수정
                System.out.println("ResultSet 가져오기 성공");

                // ResultSet 메타데이터 확인
                ResultSetMetaData metaData = rs.getMetaData();
                System.out.println("컬럼 개수: " + metaData.getColumnCount());
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    System.out.println("컬럼 " + i + ": " + metaData.getColumnName(i) + " (" + metaData.getColumnTypeName(i) + ")");
                }

                if (rs.next()) {
                    System.out.println("ResultSet에 데이터 존재");
                    CancelResvResponse response = new CancelResvResponse();

                    try {
                        response.setId(rs.getLong("ID"));
                        System.out.println("ID 설정 완료");
                    } catch (Exception e) {
                        System.out.println("ID 설정 실패: " + e.getMessage());
                        throw e;
                    }

                    try {
                        response.setUserId(rs.getLong("USER_ID"));
                        System.out.println("USER_ID 설정 완료");
                    } catch (Exception e) {
                        System.out.println("USER_ID 설정 실패: " + e.getMessage());
                        throw e;
                    }

                    try {
                        response.setBookId(rs.getLong("BOOK_ID"));
                        System.out.println("BOOK_ID 설정 완료");
                    } catch (Exception e) {
                        System.out.println("BOOK_ID 설정 실패: " + e.getMessage());
                        throw e;
                    }

                    try {
                        response.setResvDate(rs.getTimestamp("RESV_DATE").toLocalDateTime());
                        System.out.println("RESV_DATE 설정 완료");
                    } catch (Exception e) {
                        System.out.println("RESV_DATE 설정 실패: " + e.getMessage());
                        throw e;
                    }

                    try {
                        response.setStatus(rs.getString("STATUS"));
                        System.out.println("STATUS 설정 완료");
                    } catch (Exception e) {
                        System.out.println("STATUS 설정 실패: " + e.getMessage());
                        throw e;
                    }

                    System.out.println("응답 객체 생성 완료: " + response);
                    return response;
                } else {
                    System.out.println("ResultSet이 비어있음!");
                }
            }

            throw new IllegalArgumentException("예약 취소가 불가능합니다.");
        } catch (SQLException e) {
            System.out.println("=== SQLException 발생 ===");
            System.out.println("Error Code: " + e.getErrorCode());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Message: " + e.getMessage());

            String errorMessage = e.getMessage();

            // Oracle 사용자 정의 예외 처리
            if (errorMessage.contains("ORA-20001")) {
                throw new IllegalArgumentException("존재하지 않는 예약입니다.");
            } else if (errorMessage.contains("ORA-20004")) {
                throw new IllegalArgumentException("해당 예약을 찾을 수 없습니다. (not_found_exception)");
            } else if (errorMessage.contains("ORA-01403") || errorMessage.contains("no data found")) {
                throw new IllegalArgumentException("해당 예약을 찾을 수 없습니다.");
            } else {
                throw new RuntimeException("예약 취소 중 오류가 발생했습니다: " + errorMessage, e);
            }
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
