package com.database.booktrace.service;

import com.database.booktrace.dto.LoanDTO;
import com.database.booktrace.dto.response.CancelResvResponse;
import com.database.booktrace.dto.response.ExtendLoanResponse;
import com.database.booktrace.dto.response.LoanResponse;
import com.database.booktrace.entity.Loan;
import com.database.booktrace.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;

    /**
     * 사용자의 대출 목록을 조회합니다.
     * @param userId 사용자 ID
     * @return 대출 응답 목록
     */
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