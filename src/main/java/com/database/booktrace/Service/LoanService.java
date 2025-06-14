package com.database.booktrace.Service;



import com.database.booktrace.Dto.Request.LoanRequestDTO;

import com.database.booktrace.Dto.Response.CancelResvResponse;
import com.database.booktrace.Dto.Response.ExtendLoanResponse;
import com.database.booktrace.Dto.Response.LoanResponse;
import com.database.booktrace.Dto.Response.LoanResponseDTO;

import com.database.booktrace.Repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanService {

    private final LoanRepository loanRepository;


     //도서 대출 - PL/SQL 프로시저 사용
    @Transactional
    public LoanResponseDTO borrowBook(LoanRequestDTO request) {
        log.info("도서 대출 요청: userId={}, bookId={}, libraryId={}",
                request.getUserId(), request.getBookId(), request.getLibraryId());

        // 입력값 검증
        if (request.getUserId() == null || request.getBookId() == null || request.getLibraryId() == null) {
            return LoanResponseDTO.failure("필수 입력값이 누락되었습니다.");
        }

        // PL/SQL 프로시저 호출
        LoanResponseDTO response = loanRepository.borrowBookUsingProcedure(request);

        log.info("대출 처리 완료: success={}, message={}", response.isSuccess(), response.getMessage());

        return response;
    }

    //도서 대출 가능 여부 확인

    public boolean isBookAvailable(Long bookId, Long libraryId) {
        return loanRepository.checkBookAvailability(bookId, libraryId);
    }


    @Transactional(readOnly = true)
    public List<LoanResponse> getLoansByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 null일 수 없습니다.");
        }

        return loanRepository.findByUserId(userId);
    }

    public CancelResvResponse cancelResv(Long resvId){
        return loanRepository.cancelReservation(resvId);
    }

    public ExtendLoanResponse extendLoan(Long loanId){
        return loanRepository.extendLoan(loanId);
    }


}