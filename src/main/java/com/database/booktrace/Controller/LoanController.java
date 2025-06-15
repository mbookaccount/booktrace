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

//    /**
//     * 도서 대출 API (POST - JSON Body 방식)
//     *
//     */
//    @PostMapping("/borrow")
//    public ResponseEntity<LoanResponseDTO> borrowBook(@RequestBody LoanRequestDTO request) {
//        log.info("도서 대출 API 호출: userId={}, bookId={}, libraryId={}",
//                request.getUserId(), request.getBookId(), request.getLibraryId());
//
//        try {
//            // 입력값 검증
//            if (request.getUserId() == null || request.getBookId() == null || request.getLibraryId() == null) {
//                LoanResponseDTO errorResponse = LoanResponseDTO.failure("필수 입력값이 누락되었습니다.");
//                return ResponseEntity.badRequest().body(errorResponse);
//            }
//
//            // 대출 처리
//            LoanResponseDTO response = loanService.borrowBook(request);
//
//            if (response.isSuccess()) {
//                return ResponseEntity.ok(response);
//            } else {
//                return ResponseEntity.badRequest().body(response);
//            }
//
//        } catch (Exception e) {
//            log.error("도서 대출 처리 중 오류 발생", e);
//            LoanResponseDTO errorResponse = LoanResponseDTO.failure("서버 오류가 발생했습니다: " + e.getMessage());
//            return ResponseEntity.internalServerError().body(errorResponse);
//        }
//    }
//
//    /**
//     * 도서 대출 API
//     *
//     */
//    @GetMapping("/borrow")
//    public ResponseEntity<LoanResponseDTO> borrowBookByParams(
//            @RequestParam Long userId,
//            @RequestParam Long bookId,
//            @RequestParam Long libraryId) {
//
//        log.info("도서 대출 API (GET) 호출: userId={}, bookId={}, libraryId={}", userId, bookId, libraryId);
//
//        // DTO 생성
//        LoanRequestDTO request = new LoanRequestDTO(userId, bookId, libraryId);
//
//        // POST 방식과 동일한 로직 재사용
//        return borrowBook(request);
//    }
//
//    /**
//     * 도서 대출 가능 여부 확인 API
//     */
//    @GetMapping("/check-availability")
//    public ResponseEntity<Boolean> checkBookAvailability(
//            @RequestParam Long bookId,
//            @RequestParam Long libraryId) {
//
//        log.info("도서 대출 가능 여부 확인: bookId={}, libraryId={}", bookId, libraryId);
//
//        try {
//            boolean available = loanService.isBookAvailable(bookId, libraryId);
//            return ResponseEntity.ok(available);
//        } catch (Exception e) {
//            log.error("도서 대출 가능 여부 확인 중 오류", e);
//            return ResponseEntity.internalServerError().body(false);
//        }
//    }

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
            log.info("loans : " + loans.toString());
            return ResponseEntity.ok(loans);
        } catch (IllegalArgumentException e) {
            log.info("loans error : " + e.getMessage());
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


}