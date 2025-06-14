package com.database.booktrace.Repository;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
@Slf4j
public class LoanRepository {

    private final DataSource dataSource;

    public Map<String, Object> advancedBorrowEbook(Long userId, Long bookId) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("BORROW_BOOK")
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
}
