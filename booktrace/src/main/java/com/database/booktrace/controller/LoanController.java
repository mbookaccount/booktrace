package com.database.booktrace.controller;

import com.database.booktrace.dto.ErrorResponse;
import com.database.booktrace.dto.LoanDTO;
import com.database.booktrace.dto.LoanExtensionDTO;
import com.database.booktrace.dto.LoanStatusDTO;
import com.database.booktrace.dto.ReservationCancellationDTO;
import com.database.booktrace.service.LoanService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @GetMapping
    public ResponseEntity<?> getLoanStatus(HttpSession session) {
        // 세션 체크
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            ErrorResponse error = new ErrorResponse(
                false,
                "로그인이 필요합니다.",
                "Unauthorized"
            );
            return ResponseEntity.status(401).body(error);
        }

        try {
            List<LoanStatusDTO> loanStatus = loanService.getLoanStatus(userId);
            return ResponseEntity.ok(loanStatus);
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(
                false,
                "대출 현황을 조회하는 중 오류가 발생했습니다.",
                e.getMessage()
            );
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/extend")
    public ResponseEntity<?> extendLoan(
            @RequestBody LoanExtensionDTO request,
            HttpSession session) {
        // 세션 체크
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            ErrorResponse error = new ErrorResponse(
                false,
                "로그인이 필요합니다.",
                "Unauthorized"
            );
            return ResponseEntity.status(401).body(error);
        }

        // 요청 유효성 검사
        if (request.getLoanId() == null) {
            ErrorResponse error = new ErrorResponse(
                false,
                "대출 ID를 입력해주세요.",
                "BadRequest"
            );
            return ResponseEntity.badRequest().body(error);
        }

        try {
            LoanDTO loanDTO = loanService.extendLoan(request.getLoanId());
            return ResponseEntity.ok(loanDTO);
        } catch (IllegalArgumentException e) {
            ErrorResponse error = new ErrorResponse(
                false,
                e.getMessage(),
                "BadRequest"
            );
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/cancel")
    public ResponseEntity<?> cancelReservation(
            @RequestBody ReservationCancellationDTO request,
            HttpSession session) {
        // 세션 체크
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            ErrorResponse error = new ErrorResponse(
                false,
                "로그인이 필요합니다.",
                "Unauthorized"
            );
            return ResponseEntity.status(401).body(error);
        }

        // 요청 유효성 검사
        if (request.getReservationId() == null) {
            ErrorResponse error = new ErrorResponse(
                false,
                "예약 ID를 입력해주세요.",
                "BadRequest"
            );
            return ResponseEntity.badRequest().body(error);
        }

        try {
            loanService.cancelReservation(request.getReservationId());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            ErrorResponse error = new ErrorResponse(
                false,
                e.getMessage(),
                "BadRequest"
            );
            return ResponseEntity.badRequest().body(error);
        }
    }
} 