package com.database.booktrace.Controller;

import com.database.booktrace.Dto.Request.LoanRequestDTO;
import com.database.booktrace.Dto.Response.LoanResponseDTO;
import com.database.booktrace.Service.LoanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // 프론트엔드 연동을 위한 CORS 설정
public class LoanController {

    private final LoanService loanService;

    /**
     * 도서 대출 API (POST - JSON Body 방식)
     *
     */
    @PostMapping("/borrow")
    public ResponseEntity<LoanResponseDTO> borrowBook(@RequestBody LoanRequestDTO request) {
        log.info("도서 대출 API 호출: userId={}, bookId={}, libraryId={}",
                request.getUserId(), request.getBookId(), request.getLibraryId());

        try {
            // 입력값 검증
            if (request.getUserId() == null || request.getBookId() == null || request.getLibraryId() == null) {
                LoanResponseDTO errorResponse = LoanResponseDTO.failure("필수 입력값이 누락되었습니다.");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // 대출 처리
            LoanResponseDTO response = loanService.borrowBook(request);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            log.error("도서 대출 처리 중 오류 발생", e);
            LoanResponseDTO errorResponse = LoanResponseDTO.failure("서버 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 도서 대출 API
     *
     */
    @GetMapping("/borrow")
    public ResponseEntity<LoanResponseDTO> borrowBookByParams(
            @RequestParam Long userId,
            @RequestParam Long bookId,
            @RequestParam Long libraryId) {

        log.info("도서 대출 API (GET) 호출: userId={}, bookId={}, libraryId={}", userId, bookId, libraryId);

        // DTO 생성
        LoanRequestDTO request = new LoanRequestDTO(userId, bookId, libraryId);

        // POST 방식과 동일한 로직 재사용
        return borrowBook(request);
    }

    /**
     * 도서 대출 가능 여부 확인 API
     */
    @GetMapping("/check-availability")
    public ResponseEntity<Boolean> checkBookAvailability(
            @RequestParam Long bookId,
            @RequestParam Long libraryId) {

        log.info("도서 대출 가능 여부 확인: bookId={}, libraryId={}", bookId, libraryId);

        try {
            boolean available = loanService.isBookAvailable(bookId, libraryId);
            return ResponseEntity.ok(available);
        } catch (Exception e) {
            log.error("도서 대출 가능 여부 확인 중 오류", e);
            return ResponseEntity.internalServerError().body(false);
        }
    }




}