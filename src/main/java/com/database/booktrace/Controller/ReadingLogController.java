package com.database.booktrace.Controller;

import com.database.booktrace.Dto.Response.ErrorResponse;
import com.database.booktrace.Dto.Response.ReadingLogResponse;
import com.database.booktrace.Service.ReadingLogService;
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

    /**
     * 사용자의 독서 로그 목록을 조회합니다.
     * @param session HTTP 세션
     * @return 독서 로그 응답 목록
     */
    @GetMapping("/reading-log")
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
            List<ReadingLogResponse> logs = readingLogService.getReadingLogsByUserId(userId);
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
