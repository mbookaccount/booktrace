package com.database.booktrace.service;

import com.database.booktrace.dto.UserDTO;
import com.database.booktrace.entity.User;
import com.database.booktrace.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserDTO getUserInfo(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        return userRepository.findByUsername(username)
                .map(this::convertToDTO)
                .orElse(null);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserName(user.getUserName());
        dto.setLoginId(user.getLoginId());
        dto.setMileage(user.getMileage());
        dto.setInterests(user.getInterests());
        return dto;
    }
} 