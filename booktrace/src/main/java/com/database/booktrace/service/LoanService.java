package com.database.booktrace.service;

import com.database.booktrace.dto.LoanDTO;
import com.database.booktrace.dto.LoanStatusDTO;
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

    public List<LoanStatusDTO> getLoanStatus(Long userId) {
        List<Loan> loans = loanRepository.findByUserId(userId);
        return loans.stream()
                .map(this::convertToStatusDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public LoanDTO extendLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("대출 정보를 찾을 수 없습니다."));

        // 대출 상태가 아닌 경우
        if (!loan.getStatus().equals("NORMAL")) {
            throw new IllegalArgumentException("대출 상태가 아닙니다.");
        }

        // 연장 가능 여부 확인
        if (!canExtendLoan(loan)) {
            throw new IllegalArgumentException("대출 연장이 불가능합니다.");
        }

        // 다른 사람의 예약이 있는지 확인
        boolean hasReservation = loanRepository.existsReservationForBook(loan.getBook().getBookId());
        if (hasReservation) {
            throw new IllegalArgumentException("다른 사람이 예약한 도서는 연장할 수 없습니다.");
        }

        // 연장 처리
        loan.setReturnDate(loan.getReturnDate().plusDays(7));
        loan.setExtendNumber(loan.getExtendNumber() + 1);

        Loan savedLoan = loanRepository.save(loan);
        return convertToDTO(savedLoan);
    }

    @Transactional
    public void cancelReservation(Long reservationId) {
        Loan reservation = loanRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약 정보를 찾을 수 없습니다."));

        // 예약 상태가 아닌 경우
        if (!reservation.getStatus().equals("RESERVED")) {
            throw new IllegalArgumentException("예약 상태가 아닙니다.");
        }

        // 예약 취소 처리
        loanRepository.deleteById(reservationId);
    }

    private boolean canExtendLoan(Loan loan) {
        // 연장 횟수 체크 (최대 2회)
        if (loan.getExtendNumber() >= 2) {
            return false;
        }

        // 연체 체크
        if (loan.getReturnDate().isBefore(LocalDate.now())) {
            return false;
        }

        return true;
    }

    private LoanStatusDTO convertToStatusDTO(Loan loan) {
        LoanStatusDTO dto = new LoanStatusDTO();
        dto.setLoanNumber(loan.getLoanId());
        dto.setBorrowDate(loan.getBorrowDate().atStartOfDay());
        dto.setReturnDate(loan.getReturnDate().atStartOfDay());
        dto.setBookTitle(loan.getBook().getTitle());
        dto.setLibraryName(loan.getBook().getLibrary().getName());
        dto.setStatus(loan.getStatus());
        dto.setExtensionCount(loan.getExtendNumber());
        return dto;
    }

    private LoanDTO convertToDTO(Loan loan) {
        LoanDTO dto = new LoanDTO();
        dto.setId(loan.getLoanId());
        dto.setUserId(loan.getUser().getUserId());
        dto.setBookId(loan.getBook().getBookId());
        dto.setBorrowDate(loan.getBorrowDate().atStartOfDay());
        dto.setReturnDate(loan.getReturnDate().atStartOfDay());
        dto.setStatus(loan.getStatus());
        dto.setExtensionCount(loan.getExtendNumber());
        return dto;
    }
} 