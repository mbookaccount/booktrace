package com.database.booktrace.repository;

import com.database.booktrace.entity.User;
import com.database.booktrace.util.DatabaseConnection;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Repository
public class UserRepository {
    
    @Autowired
    private DatabaseConnection databaseConnection;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT u.user_id, u.user_name, u.login_id, u.password, u.mileage, u.interests, " +
                    "u.created_at, u.updated_at, u.is_active " +
                    "FROM users u " +
                    "WHERE u.login_id = ? AND u.is_active = 1";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getLong("user_id"));
                user.setUserName(rs.getString("user_name"));
                user.setLoginId(rs.getString("login_id"));
                user.setPassword(rs.getString("password"));
                user.setMileage(rs.getInt("mileage"));
                user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                user.setUpdatedAt(rs.getTimestamp("updated_at") != null ?
                    rs.getTimestamp("updated_at").toLocalDateTime() : null);
                user.setIsActive(rs.getBoolean("is_active"));

                // JSON 문자열을 Set으로 변환
                String interestsJson = rs.getString("interests");
                if (interestsJson != null && !interestsJson.isEmpty()) {
                    Set<String> interests = objectMapper.readValue(interestsJson, new TypeReference<HashSet<String>>() {});
                    user.setInterests(interests);
                }

                return Optional.of(user);
            }

            return Optional.empty();

        } catch (SQLException | JsonProcessingException e) {
            throw new RuntimeException("Error finding user by username", e);
        }
    }

    public User save(User user) {
        String sql = "INSERT INTO users (user_name, login_id, password, mileage, interests, created_at, updated_at, is_active) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getUserName());
            pstmt.setString(2, user.getLoginId());
            pstmt.setString(3, user.getPassword());
            pstmt.setInt(4, user.getMileage());
            
            // Set을 JSON 문자열로 변환
            String InterestsJson = user.getInterests() != null ?
                objectMapper.writeValueAsString(user.getInterests()) : null;
            pstmt.setString(5, InterestsJson);
            
            pstmt.setTimestamp(6, Timestamp.valueOf(user.getCreatedAt()));
            pstmt.setTimestamp(7, user.getUpdatedAt() != null ? 
                Timestamp.valueOf(user.getUpdatedAt()) : null);
            pstmt.setBoolean(8, user.getIsActive());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setUserId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }

            return user;

        } catch (SQLException | JsonProcessingException e) {
            throw new RuntimeException("Error saving user", e);
        }
    }

    public void updateUserMileage(Long userId, int mileage) {
        String sql = "UPDATE users SET mileage = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";

        try (Connection conn = databaseConnection.getConnection();
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

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String interestsJson = objectMapper.writeValueAsString(interests);
            pstmt.setString(1, interestsJson);
            pstmt.setLong(2, userId);
            pstmt.executeUpdate();

        } catch (SQLException | JsonProcessingException e) {
            throw new RuntimeException("Error updating user interests", e);
        }
    }
} 