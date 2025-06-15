package com.database.booktrace.Service;

import com.database.booktrace.Domain.User;
import com.database.booktrace.Dto.Response.UserDTO;
import com.database.booktrace.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.stream.Collectors;
import com.database.booktrace.Domain.BookCategory;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserDTO getUserInfo(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null or empty");
        }

        return userRepository.findByUserId(userId)
                .map(this::convertToDTO)
                .orElse(null);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserName(user.getUserName());
        dto.setLoginId(user.getLoginId());
        dto.setMileage(user.getMileage());
        dto.setInterests(user.getPreferredCategories());
        return dto;
    }

    public boolean changePassword(Long userId, String currentRaw, String newRaw) {
        return userRepository.updateUserPassword(userId, currentRaw, newRaw);  // 평문 그대로 전달
    }

    public void updateInterests(Long userId, Set<String> interests) {
        Set<BookCategory> categories = interests.stream()
                .map(BookCategory::valueOf)
                .collect(Collectors.toSet());
        userRepository.updateUserInterests(userId, categories);
    }
}