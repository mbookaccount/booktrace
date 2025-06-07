package com.database.booktrace.repository;

import com.database.booktrace.dto.response.LoanResponse;
import com.database.booktrace.entity.Loan;
import com.database.booktrace.util.DatabaseConnection;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import oracle.jdbc.OracleTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class LoanRepository {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private DatabaseConnection databaseConnection;

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
        try (Connection conn = databaseConnection.getConnection();
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
    public Loan extendLoan(Long loanId) {
        try (Connection conn = databaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall(
                "{ call loan_package.extend_loan(?, ?) }"
             )) {

            cs.setLong(1, loanId);
            cs.registerOutParameter(2, OracleTypes.CURSOR);
            cs.execute();

            ResultSet rs = (ResultSet) cs.getObject(2);

            throw new IllegalArgumentException("대출 연장이 불가능합니다.");

        } catch (SQLException e) {
            handleSQLException(e, "대출 연장 중 오류가 발생했습니다.");
            return null;
        }
    }

    // 예약 취소
    public void cancelReservation(Long reservationId) {
        try (Connection conn = databaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall(
                "{ call loan_package.cancel_reservation(?) }"
             )) {

            cs.setLong(1, reservationId);
            cs.execute();

        } catch (SQLException e) {
            handleSQLException(e, "예약 취소 중 오류가 발생했습니다.");
        }
    }

    // 도서 예약 여부 확인
    public boolean hasReservation(Long bookId) {
        try (Connection conn = databaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall(
                "{ ? = call loan_package.has_reservation(?) }"
             )) {

            cs.registerOutParameter(1, OracleTypes.NUMBER);
            cs.setLong(2, bookId);
            cs.execute();

            return cs.getInt(1) == 1;

        } catch (SQLException e) {
            handleSQLException(e, "예약 여부 확인 중 오류가 발생했습니다.");
            return false;
        }
    }

    // 대출 정보 저장
    public Loan saveLoan(Loan loan) {
        try (Connection conn = databaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall(
                "{ ? = call loan_package.save_loan(?, ?, ?, ?, ?, ?, ?) }"
             )) {

            cs.registerOutParameter(1, OracleTypes.NUMBER);
            cs.setLong(2, loan.getLoanId());
            cs.setLong(3, loan.getUserId());
            cs.setLong(4, loan.getBookId());
            cs.setTimestamp(5, Timestamp.valueOf(loan.getBorrowDate()));
            cs.setTimestamp(6, Timestamp.valueOf(loan.getReturnDate()));
            cs.setInt(7, loan.getExtendNumber());
            cs.setString(8, loan.getStatus());
            
            cs.execute();
            
            loan.setLoanId(cs.getLong(1));
            return loan;

        } catch (SQLException e) {
            handleSQLException(e, "대출 정보 저장 중 오류가 발생했습니다.");
            return null;
        }
    }

    // 대출 정보 삭제
    public void deleteLoan(Long loanId) {
        try (Connection conn = databaseConnection.getConnection();
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
        try (Connection conn = databaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall(
                "{ ? = call loan_package.can_extend_loan(?) }"
             )) {

            cs.registerOutParameter(1, OracleTypes.NUMBER);
            cs.setLong(2, loanId);
            cs.execute();

            return cs.getInt(1) == 1;

        } catch (SQLException e) {
            handleSQLException(e, "대출 연장 가능 여부 확인 중 오류가 발생했습니다.");
            return false;
        }
    }

    private void handleSQLException(SQLException e, String message) {
        // Oracle 에러 코드에 따른 예외 처리
        switch (e.getErrorCode()) {
            case 20001: // 대출 연장 횟수 초과
                throw new IllegalArgumentException("대출 연장은 최대 2회까지만 가능합니다.");
            case 20002: // 연체된 대출
                throw new IllegalStateException("연체된 대출은 연장할 수 없습니다.");
            case 20003: // 이미 예약된 도서
                throw new IllegalStateException("이미 예약된 도서입니다.");
            case 20004: // 대출 정보 없음
                throw new IllegalArgumentException("대출 정보를 찾을 수 없습니다.");
            default:
                throw new RuntimeException(message, e);
        }
    }
}