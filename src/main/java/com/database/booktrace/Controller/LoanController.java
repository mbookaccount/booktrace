package com.database.booktrace.Controller;

import com.database.booktrace.Dto.Request.LoanRequestDTO;
import com.database.booktrace.Dto.Response.LoanResponseDTO;
import com.database.booktrace.Service.LoanService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/loan")
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
public class LoanController {

    private final LoanService loanService;

    @PostMapping("/book/{bookId}")
    public ResponseEntity<?> advancedBorrowEbook(@PathVariable Long bookId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "로그인이 필요합니다."
            ));
        }

        try {
            Map<String, Object> result = loanService.advancedBorrowEbook(userId, bookId);

            // Oracle NUMBER 타입은 BigDecimal로 반환됨! 안전한 변환 필요
            Object resultObj = result.get("p_result");
            Object loanIdObj = result.get("p_loan_id");
            String message = (String) result.get("p_message");

            // BigDecimal → Integer/Long 안전 변환
            int status = 0;
            if (resultObj instanceof java.math.BigDecimal) {
                status = ((java.math.BigDecimal) resultObj).intValue();
            } else if (resultObj instanceof Integer) {
                status = (Integer) resultObj;
            }

            Long loanId = null;
            if (loanIdObj instanceof java.math.BigDecimal) {
                loanId = ((java.math.BigDecimal) loanIdObj).longValue();
            } else if (loanIdObj instanceof Long) {
                loanId = (Long) loanIdObj;
            }

            String safeMessage = (message != null) ? message : "알 수 없는 오류";

            log.info("****결과로그 여기***** 결과 -> {} 메세지 --> {} loanId--->{}",
                    status, safeMessage, loanId);

            // HashMap 사용 (null 허용)
            Map<String, Object> response = new HashMap<>();
            response.put("success", status == 1);
            response.put("message", safeMessage);
            if (loanId != null) {
                response.put("loanId", loanId);
            }
            // 상태 코드 구분
            if (status == 1) {
                return ResponseEntity.ok(response);
            } else if (safeMessage.contains("대출 한도 초과")) {
                return ResponseEntity.status(403).body(response); // Forbidden
            } else if (safeMessage.contains("대출 불가능한 도서")) {
                return ResponseEntity.status(409).body(response); // Conflict
            } else if (safeMessage.contains("존재하지 않는 도서")) {
                return ResponseEntity.status(404).body(response); // Not Found
            } else {
                return ResponseEntity.status(400).body(response); // 기타 실패
            }

        } catch (Exception e) {
            log.error("대출 처리 중 오류 발생", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "대출 처리 중 오류가 발생했습니다."
            ));
        }
    }
}