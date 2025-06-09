package com.database.booktrace.controller;

import com.database.booktrace.dto.request.CancelResvRequest;
import com.database.booktrace.dto.request.ExtendLoanRequest;
import com.database.booktrace.dto.response.CancelResvResponse;
import com.database.booktrace.dto.response.ErrorResponse;
import com.database.booktrace.dto.response.ExtendLoanResponse;
import com.database.booktrace.dto.response.LoanResponse;
import com.database.booktrace.service.LoanService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    @Autowired
    private LoanService loanService;

    /**
     * 사용자의 대출 목록을 조회합니다.
     * @param session HTTP 세션
     * @return 대출 응답 목록
     */
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
    public ResponseEntity<?> cancelResv(CancelResvRequest request){
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
    public ResponseEntity<?> extendLoan(ExtendLoanRequest request){
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