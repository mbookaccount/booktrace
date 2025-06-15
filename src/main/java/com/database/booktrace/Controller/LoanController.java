package com.database.booktrace.Controller;

import com.database.booktrace.Dto.Request.CancelResvRequest;
import com.database.booktrace.Dto.Request.ExtendLoanRequest;
import com.database.booktrace.Dto.Request.LoanRequestDTO;
import com.database.booktrace.Dto.Response.*;
import com.database.booktrace.Service.LoanService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
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
            Map<String, Object> result = loanService.BorrowEbook(userId, bookId);

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


    @GetMapping
    public ResponseEntity<?> getMyLoans(HttpSession session) {
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
            List<LoanResponse> loans = loanService.getLoansByUserId(userId);
            return ResponseEntity.ok(loans);
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

    @DeleteMapping("/cancel")
    public ResponseEntity<?> cancelResv(@RequestBody CancelResvRequest request){
        try{
            CancelResvResponse cancelResvResponse = loanService.cancelResv(request.getReservationId());
            return ResponseEntity.ok(cancelResvResponse);
        } catch (IllegalArgumentException e){
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

    @PostMapping("/extend")
    public ResponseEntity<?> extendLoan(@RequestBody ExtendLoanRequest request){
        try{
            ExtendLoanResponse extendLoanResponse = loanService.extendLoan(request.getLoanId());
            return ResponseEntity.ok(extendLoanResponse);
        } catch (IllegalArgumentException e){
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

    @PostMapping("/{loanId}/return")
    public ResponseEntity<Map<String, Object>> returnBook(@PathVariable Long loanId) {
        try {
            log.info("반납 API 호출 - 대출 ID: {}", loanId);

            Map<String, Object> result = loanService.returnBook(loanId);

            Integer resultCode = (Integer) result.get("p_result");
            String message = (String) result.get("p_message");

            log.info("반납 처리 완료 - 결과: {}, 메시지: {}", resultCode, message);

            if (resultCode != null && resultCode == 1) {
                // 성공
                return ResponseEntity.ok(result);
            } else {
                // 실패 (비즈니스 로직 오류)
                log.warn("반납 실패 - 대출 ID: {}, 메시지: {}", loanId, message);
                return ResponseEntity.badRequest().body(result);
            }

        } catch (IllegalArgumentException e) {
            log.error("반납 요청 오류 - 대출 ID: {}, 오류: {}", loanId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "p_result", 0,
                            "p_message", e.getMessage(),
                            "error", "INVALID_REQUEST"
                    ));
        } catch (Exception e) {
            log.error("반납 처리 중 시스템 오류 - 대출 ID: {}", loanId, e);
            return ResponseEntity.status(500)
                    .body(Map.of(
                            "p_result", 0,
                            "p_message", "서버 내부 오류가 발생했습니다.",
                            "error", "INTERNAL_SERVER_ERROR"
                    ));
        }
    }


}