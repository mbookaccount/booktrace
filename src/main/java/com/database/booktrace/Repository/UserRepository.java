
package com.database.booktrace.Repository;

import com.database.booktrace.Domain.BookCategory;
import com.database.booktrace.Domain.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepository  {
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final ObjectMapper objectMapper;

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

    public Long loginUser(String loginId, String password) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("LOGIN_USER_SIMPLE")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_login_id", Types.VARCHAR),
                        new SqlParameter("p_password", Types.VARCHAR),
                        new SqlOutParameter("p_user_id", Types.NUMERIC)
                );

        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_login_id", loginId);
        inParams.put("p_password", password);

        Map<String, Object> out = jdbcCall.execute(inParams);

        Object userIdObj = out.get("p_user_id");
        return (userIdObj != null) ? ((Number) userIdObj).longValue() : null;
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

    // ==============================================

    public Optional<User> findByUsername(String username) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("GET_USER_BY_USERNAME")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_login_id", Types.VARCHAR),
                        new SqlOutParameter("p_user_id", Types.NUMERIC),
                        new SqlOutParameter("p_user_name", Types.VARCHAR),
                        new SqlOutParameter("p_password", Types.VARCHAR),
                        new SqlOutParameter("p_mileage", Types.NUMERIC),
                        new SqlOutParameter("p_interests", Types.VARCHAR),
                        new SqlOutParameter("p_created_at", Types.TIMESTAMP),
                        new SqlOutParameter("p_updated_at", Types.TIMESTAMP),
                        new SqlOutParameter("p_is_active", Types.CHAR)
                );

        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_login_id", username);

        Map<String, Object> out = jdbcCall.execute(inParams);

        if (out.get("p_user_id") == null) {
            return Optional.empty();
        }

        User user = new User();
        user.setUserId(((Number) out.get("p_user_id")).longValue());
        user.setUserName((String) out.get("p_user_name"));
        user.setLoginId(username);
        user.setPassword((String) out.get("p_password"));
        user.setMileage(((Number) out.get("p_mileage")).intValue());

        String interestsJson = (String) out.get("p_interests");
        if (interestsJson != null && !interestsJson.isEmpty()) {
            try {
                Set<BookCategory> interests = objectMapper.readValue(interestsJson, new TypeReference<HashSet<BookCategory>>() {});
                user.setPreferredCategories(interests);
            } catch (JsonProcessingException e) {
                log.error("Error parsing interests JSON: {}", e.getMessage());
            }
        }

        user.setCreatedAt(((Timestamp) out.get("p_created_at")).toLocalDateTime());
        Timestamp updatedAt = (Timestamp) out.get("p_updated_at");
        user.setUpdatedAt(updatedAt != null ? updatedAt.toLocalDateTime() : null);
        user.setIsActive(((String) out.get("p_is_active")).charAt(0));

        return Optional.of(user);
    }

    public User save(User user) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("SAVE_USER")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_user_name", Types.VARCHAR),
                        new SqlParameter("p_login_id", Types.VARCHAR),
                        new SqlParameter("p_password", Types.VARCHAR),
                        new SqlParameter("p_mileage", Types.NUMERIC),
                        new SqlParameter("p_interests", Types.VARCHAR),
                        new SqlParameter("p_created_at", Types.TIMESTAMP),
                        new SqlParameter("p_updated_at", Types.TIMESTAMP),
                        new SqlParameter("p_is_active", Types.CHAR),
                        new SqlOutParameter("p_user_id", Types.NUMERIC)
                );

        Map<String, Object> inParams = new HashMap<>();
        inParams.put("p_user_name", user.getUserName());
        inParams.put("p_login_id", user.getLoginId());
        inParams.put("p_password", user.getPassword());
        inParams.put("p_mileage", user.getMileage());

        try {
            String interestsJson = user.getPreferredCategories() != null ?
                    objectMapper.writeValueAsString(user.getPreferredCategories()) : null;
            inParams.put("p_interests", interestsJson);
        } catch (JsonProcessingException e) {
            log.error("Error converting interests to JSON: {}", e.getMessage());
            inParams.put("p_interests", null);
        }

        inParams.put("p_created_at", Timestamp.valueOf(user.getCreatedAt()));
        inParams.put("p_updated_at", user.getUpdatedAt() != null ? Timestamp.valueOf(user.getUpdatedAt()) : null);
        inParams.put("p_is_active", String.valueOf(user.getIsActive()));

        Map<String, Object> out = jdbcCall.execute(inParams);
        user.setUserId(((Number) out.get("p_user_id")).longValue());
        return user;
    }

    public void updateUserMileage(Long userId, int mileage) {
        String sql = "UPDATE users SET mileage = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, mileage);
            pstmt.setLong(2, userId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating user mileage", e);
        }
    }

    public void updateUserInterests(Long userId, Set<String> interests) {
        String sql = "UPDATE users SET interests = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String interestsJson = objectMapper.writeValueAsString(interests);
            pstmt.setString(1, interestsJson);
            pstmt.setLong(2, userId);
            pstmt.executeUpdate();

        } catch (SQLException | JsonProcessingException e) {
            throw new RuntimeException("Error updating user interests", e);
        }
    }

    public boolean updateUserPassword(Long userId, String newPasswordPlain) {
        String updateSql = "UPDATE users SET password = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ? AND is_active = 1";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

            updateStmt.setString(1, newPasswordPlain); // 평문 그대로 저장
            updateStmt.setLong(2, userId);
            int affectedRows = updateStmt.executeUpdate();

            return affectedRows > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error updating user password", e);
        }
    }

    public Optional<User> findByUserId(Long userId) {
        String sql = "SELECT " +
                "    u.user_id, " +
                "    u.user_name, " +
                "    u.login_id, " +
                "    u.password, " +
                "    u.mileage, " +
                "    u.interests, " +
                "    u.created_at, " +
                "    u.updated_at, " +
                "    u.is_active " +
                "FROM users u " +
                "WHERE u.user_id = ? AND u.is_active = 'Y'";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getLong("user_id"));
                user.setUserName(rs.getString("user_name"));
                user.setLoginId(rs.getString("login_id"));
                user.setPassword(rs.getString("password"));
                user.setMileage(rs.getInt("mileage"));

                String interestsJson = rs.getString("interests");
                if (interestsJson != null && !interestsJson.isEmpty()) {
                    try {
                        Set<BookCategory> interests = objectMapper.readValue(interestsJson,
                                new TypeReference<HashSet<BookCategory>>() {});
                        user.setPreferredCategories(interests);
                    } catch (JsonProcessingException e) {
                        log.error("Error parsing interests JSON: {}", e.getMessage());
                    }
                }

                user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                Timestamp updatedAt = rs.getTimestamp("updated_at");
                user.setUpdatedAt(updatedAt != null ? updatedAt.toLocalDateTime() : null);
                user.setIsActive(rs.getString("is_active").charAt(0));

                return Optional.of(user);
            }
            return Optional.empty();

        } catch (SQLException e) {
            log.error("Error finding user by ID: {}", e.getMessage(), e);
            throw new RuntimeException("Error finding user by ID", e);
        }
    }
}
