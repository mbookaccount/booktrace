package com.database.booktrace.repository;

import com.database.booktrace.entity.Loan;
import com.database.booktrace.entity.Book;
import com.database.booktrace.entity.Library;
import oracle.jdbc.OracleTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class LoanRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Loan> loanRowMapper = (rs, rowNum) -> {
        Loan loan = new Loan();
        loan.setLoanId(rs.getLong("loan_id"));
        loan.setBorrowDate(rs.getTimestamp("borrow_date").toLocalDateTime());
        loan.setReturnDate(rs.getTimestamp("return_date").toLocalDateTime());
        loan.setExtendNumber(rs.getInt("extend_number"));
        loan.setStatus(rs.getString("status"));
        loan.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        loan.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return loan;
    };

    public List<Loan> findByUserId(Long userId) {
        return jdbcTemplate.execute(connection -> {
            try (CallableStatement cs = connection.prepareCall(
                "{ call loan_package.get_user_loans(?, ?) }"
            )) {
                cs.setLong(1, userId);
                cs.registerOutParameter(2, OracleTypes.CURSOR);
                cs.execute();
                
                ResultSet rs = (ResultSet) cs.getObject(2);
                List<Loan> loans = new ArrayList<>();
                while (rs.next()) {
                    loans.add(loanRowMapper.mapRow(rs, rs.getRow()));
                }
                return loans;
            } catch (SQLException e) {
                handleSQLException(e, "사용자의 대출 목록을 조회하는 중 오류가 발생했습니다.");
                return new ArrayList<>();
            }
        });
    }

    public Loan findById(Long loanId) {
        return jdbcTemplate.execute(connection -> {
            try (CallableStatement cs = connection.prepareCall(
                "{ call loan_package.get_loan_by_id(?, ?) }"
            )) {
                cs.setLong(1, loanId);
                cs.registerOutParameter(2, OracleTypes.CURSOR);
                cs.execute();
                
                ResultSet rs = (ResultSet) cs.getObject(2);
                if (rs.next()) {
                    return loanRowMapper.mapRow(rs, rs.getRow());
                }
                return null;
            } catch (SQLException e) {
                handleSQLException(e, "대출 정보를 조회하는 중 오류가 발생했습니다.");
                return null;
            }
        });
    }

    public Loan extendLoan(Long loanId) {
        return jdbcTemplate.execute(connection -> {
            try (CallableStatement cs = connection.prepareCall(
                "{ call loan_package.extend_loan(?, ?) }"
            )) {
                cs.setLong(1, loanId);
                cs.registerOutParameter(2, OracleTypes.CURSOR);
                cs.execute();
                
                ResultSet rs = (ResultSet) cs.getObject(2);
                if (rs.next()) {
                    return loanRowMapper.mapRow(rs, rs.getRow());
                }
                return null;
            } catch (SQLException e) {
                handleSQLException(e, "대출 연장 중 오류가 발생했습니다.");
                return null;
            }
        });
    }

    public void cancelReservation(Long reservationId) {
        jdbcTemplate.execute(connection -> {
            try (CallableStatement cs = connection.prepareCall(
                "{ call loan_package.cancel_reservation(?) }"
            )) {
                cs.setLong(1, reservationId);
                cs.execute();
                return null;
            } catch (SQLException e) {
                handleSQLException(e, "예약 취소 중 오류가 발생했습니다.");
                return null;
            }
        });
    }

    public boolean existsReservationForBook(Long bookId) {
        return jdbcTemplate.execute(connection -> {
            try (CallableStatement cs = connection.prepareCall(
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
        });
    }

    private void handleSQLException(SQLException e, String message) {
        // Oracle 에러 코드에 따른 예외 처리
        switch (e.getErrorCode()) {
            case 201: // 사용자 정의 예외: 대출 연장 횟수 초과
                throw new IllegalArgumentException("대출 연장은 최대 2회까지만 가능합니다.");
            case 20002: // 사용자 정의 예외: 연체된 대출
                throw new IllegalStateException("연체된 대출은 연장할 수 없습니다.");
            case 20003: // 사용자 정의 예외: 이미 예약된 도서
                throw new IllegalStateException("이미 예약된 도서입니다.");
            case 20004: // 사용자 정의 예외: 대출 정보 없음
                throw new IllegalArgumentException("대출 정보를 찾을 수 없습니다.");
            default:
                throw new RuntimeException(message, e);
        }
    }
} 