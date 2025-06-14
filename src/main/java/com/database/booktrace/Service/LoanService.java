package com.database.booktrace.Service;



import com.database.booktrace.Dto.Request.LoanRequestDTO;

import com.database.booktrace.Dto.Response.LoanResponseDTO;

import com.database.booktrace.Repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanService {

    private final LoanRepository loanRepository;

    public Map<String, Object> advancedBorrowEbook(Long userId, Long bookId) {
        log.info("대출 요청 - 사용자: {}, 도서: {}", userId, bookId);
        return loanRepository.advancedBorrowEbook(userId, bookId);
    }
}