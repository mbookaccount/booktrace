package com.database.booktrace.Controller;



import com.database.booktrace.Dto.Request.LoanRequestDTO;

import com.database.booktrace.Dto.Response.LoanResponseDTO;

import com.database.booktrace.Service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final LoanService loanService;

    /**
     * 브라우저에서 바로 테스트할 수 있는 GET API
     * 예: http://localhost:8081/test/borrow?userId=1&bookId=1&libraryId=1
     */
    @GetMapping("/borrow")
    public LoanResponseDTO testBorrow(
            @RequestParam Long userId,
            @RequestParam Long bookId,
            @RequestParam Long libraryId) {

        LoanRequestDTO request = new LoanRequestDTO(userId, bookId, libraryId);
        return loanService.borrowBook(request);
    }

    /**
     * 간단한 상태 확인
     * 예: http://localhost:8081/test/status
     */
    @GetMapping("/status")
    public String testStatus() {
        return "도서 대출 시스템이 정상 작동 중입니다!";
    }
}