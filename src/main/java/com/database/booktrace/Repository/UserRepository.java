package com.database.booktrace.Repository;


import com.database.booktrace.Domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class UserRepository  {
    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> registerUser(String userName, String loginId, String password, String confirmPassword, String preferredCategories) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("REGISTER_USER")
                .withoutProcedureColumnMetaDataAccess()  // 이거 꼭 넣어줘야 함!!!
                .declareParameters(
                        new SqlParameter("p_user_name", Types.VARCHAR),
                        new SqlParameter("p_login_id", Types.VARCHAR),
                        new SqlParameter("p_password", Types.VARCHAR),
                        new SqlParameter("p_password_confirm", Types.VARCHAR),
                        new SqlParameter("p_preferred_categories", Types.VARCHAR),
                        new SqlOutParameter("p_result", Types.INTEGER),
                        new SqlOutParameter("p_message", Types.VARCHAR),
                        new SqlOutParameter("p_user_id", Types.INTEGER)
                );

        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_user_name", userName);
        inParams.put("p_login_id", loginId);
        inParams.put("p_password", password);
        inParams.put("p_password_confirm", confirmPassword);
        inParams.put("p_preferred_categories", preferredCategories); // "TECHNOLOGY,LITERATURE"

        return jdbcCall.execute(inParams);
    }

    public int countByUserId(String userId) {
        String sql = "SELECT COUNT(*) FROM USERS WHERE LOGIN_ID = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, userId);
    }

    public int countByUserName(String userName) {
        String sql = "SELECT COUNT(*) FROM USERS WHERE USER_NAME = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, userName);
    }

    public int existsByUserId(String userId) {
        String sql = "SELECT CASE WHEN EXISTS (SELECT 1 FROM USERS WHERE LOGIN_ID = ?) THEN 1 ELSE 0 END FROM DUAL";
        return jdbcTemplate.queryForObject(sql, Integer.class, userId);
    }

    public int existsByUserName(String userName) {
        String sql = "SELECT CASE WHEN EXISTS (SELECT 1 FROM USERS WHERE USER_NAME = ?) THEN 1 ELSE 0 END FROM DUAL";
        return jdbcTemplate.queryForObject(sql, Integer.class, userName);
    }
}
