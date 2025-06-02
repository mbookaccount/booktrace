package com.database.booktrace.controller;

import com.database.booktrace.dto.ErrorResponse;
import com.database.booktrace.dto.UserDTO;
import com.database.booktrace.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(HttpSession session) {
        // 세션 체크 (401 Unauthorized)
        String username = (String) session.getAttribute("username");
        if (username == null) {
            ErrorResponse error = new ErrorResponse(
                false,
                "로그인이 필요합니다.",
                "Unauthorized"
            );
            return ResponseEntity
                    .status(401)
                    .body(error);
        }

        try {
            UserDTO userDTO = userService.getUserInfo(username);
            
            // 사용자 존재 여부 체크 (404 Not Found)
            if (userDTO == null) {
                ErrorResponse error = new ErrorResponse(
                    false,
                    "사용자 정보를 찾을 수 없습니다.",
                    "UserNotFound"
                );
                return ResponseEntity
                        .status(404)
                        .body(error);
            }

            // 성공 응답 (200 OK)
            return ResponseEntity.ok(userDTO);
            
        } catch (IllegalArgumentException e) {
            ErrorResponse error = new ErrorResponse(
                false,
                "다시 시도해주세요.",
                e.getMessage()
            );
            return ResponseEntity
                    .status(400)
                    .body(error);
        }
    }
} 