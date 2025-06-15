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
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanService {

    private final LoanRepository loanRepository;

    @Transactional(readOnly = true)
    public List<LoanResponse> getLoansByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 null일 수 없습니다.");
        }

        return loanRepository.findByUserId(userId);
    }

    public Map<String, Object> BorrowEbook(Long userId, Long bookId) {
    log.info("대출 요청 - 사용자: {}, 도서: {}", userId, bookId);
    return loanRepository.BorrowEbook(userId, bookId);
    }


    //도서 반납
    @Transactional
    public Map<String,Object> returnBook(Long loanId){
        log.info("반납 요청 : 대출id {}",loanId);

        if(loanId==null){
            throw new IllegalArgumentException("대출 ID는 null일 수 없습니다.");
        }
        return loanRepository.returnBook(loanId);
    }


    public CancelResvResponse cancelResv(Long resvId){
        return loanRepository.cancelReservation(resvId);
    }

    public ExtendLoanResponse extendLoan(Long loanId){
        return loanRepository.extendLoan(loanId);
    }


}