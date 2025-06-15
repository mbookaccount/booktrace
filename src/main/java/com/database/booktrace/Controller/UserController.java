package com.database.booktrace.Controller;

import com.database.booktrace.Dto.Request.KeywordUpdateRequest;
import com.database.booktrace.Dto.Request.PasswordChangeRequest;
import com.database.booktrace.Dto.Response.ErrorResponse;
import com.database.booktrace.Dto.Response.UserDTO;
import com.database.booktrace.Service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(HttpSession session) {
        // 세션 체크 (401 Unauthorized)
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
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
            UserDTO userDTO = userService.getUserInfo(userId);

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
            log.info("userName : "+ userDTO.getUserName());
            log.info("LoginId : "+ userDTO.getLoginId());
            log.info("getInterests : "+ userDTO.getInterests());
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

    @PutMapping("/me/password")
    public ResponseEntity<String> updatePassword(@RequestBody PasswordChangeRequest request,
                                                 HttpSession session) {
        Long userId = (Long) session.getAttribute("userId"); // 세션에서 사용자 ID 가져오기
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        boolean success = userService.changePassword(userId, request.getCurrentPassword(), request.getNewPassword());
        return success ? ResponseEntity.ok("비밀번호 변경 성공")
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body("현재 비밀번호가 일치하지 않습니다.");
    }

    @PutMapping("/me/preferred-categories")
    public ResponseEntity<String> updateKeywords(@RequestBody KeywordUpdateRequest request,
                                                 HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        userService.updateInterests(userId, request.getKeywords());
        return ResponseEntity.ok("관심 키워드가 저장되었습니다.");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session, HttpServletResponse response) {
        // 세션에서 사용자 ID 가져오기
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            ErrorResponse error = new ErrorResponse(
                false,
                "이미 로그아웃된 상태입니다.",
                "AlreadyLoggedOut"
            );
            return ResponseEntity
                .status(400)
                .body(error);
        }

        // 세션의 모든 속성 제거
        Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();
            session.removeAttribute(attributeName);
        }

        // 세션 무효화
        session.invalidate();
        
        // JSESSIONID 쿠키 제거
        Cookie jsessionCookie = new Cookie("JSESSIONID", null);
        jsessionCookie.setPath("/");
        jsessionCookie.setMaxAge(0);
        jsessionCookie.setHttpOnly(true);
        response.addCookie(jsessionCookie);

        // 추가 쿠키 제거 (프론트엔드에서 사용하는 쿠키들)
        String[] cookieNames = {"userId", "userName", "loginId", "userInfo"};
        for (String cookieName : cookieNames) {
            Cookie cookie = new Cookie(cookieName, null);
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }
        
        log.info("User ID: {} logged out successfully", userId);
        
        // 로그아웃 성공 응답에 추가 정보 포함
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("success", true);
        responseBody.put("message", "로그아웃이 완료되었습니다.");
        responseBody.put("error", "LogoutSuccess");
        responseBody.put("requireRefresh", true);
        responseBody.put("redirectUrl", "/");  // 홈으로 리다이렉트
        
        return ResponseEntity.ok(responseBody);
    }
    
}