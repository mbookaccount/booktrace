package com.database.booktrace.controller;

import com.database.booktrace.dto.ErrorResponse;
import com.database.booktrace.dto.ReadingLogDTO;
import com.database.booktrace.service.ReadingLogService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ReadingLogController {

    @Autowired
    private ReadingLogService readingLogService;

    @GetMapping("/reading-logs")
    public ResponseEntity<?> getMyReadingLogs(HttpSession session) {
        // 세션에서 사용자 ID 가져오기
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
            List<ReadingLogDTO> logs = readingLogService.getReadingLogsByUserId(userId);
            return ResponseEntity.ok(logs);
        } catch (IllegalArgumentException e) {
            ErrorResponse error = new ErrorResponse(
                false,
                "다시 시도해주세요.",
                e.getMessage()
            );
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(error);
        }
    }
}
