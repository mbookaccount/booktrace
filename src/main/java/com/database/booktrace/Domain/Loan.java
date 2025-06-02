package com.database.booktrace.Domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "LOANS")
@SequenceGenerator(
        name = "loan_seq_gen",
        sequenceName = "seq_loan_id",
        initialValue = 1,
        allocationSize = 1
)
public class Loan extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "loan_seq_gen")
    @Column(name = "LOG_ID")  // ERD에서 log_id로 되어있으니 맞춤
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BOOK_ID", nullable = false)
    private Book book;

    @Column(name = "BORROW_DATE") //대출 일자
    private LocalDateTime borrowDate;

    @Column(name = "DUE_DATE") //반납 예정일
    private LocalDateTime dueDate;

    @Column(name = "RETURN_DATE") //반납 일자
    private LocalDateTime returnDate;

    @Column(name = "EXTEND_NUMBER") //연장 횟수
    @Builder.Default
    private Integer extendNumber = 0;

    @Column(name = "STATUS", length = 20)
    @Builder.Default
    private String status = "BORROWED"; // BORROWED, RETURNED

    // 만료 여부
    public boolean isExpired() {
        if (isBorrowed()) return false;
        return dueDate != null && dueDate.isBefore(LocalDateTime.now());
    }

    //
    public Boolean isBorrowed(){
        return this.status.equals("BORROWED");
    }

    // 연장 가능 여부 수정
    public boolean canExtend() {
        return extendNumber < 2 && !isExpired() && "BORROWED".equals(status);
    }

    // 자동 만료 처리
    public void expire() {
        if (isExpired() && "BORROWED".equals(status)) {
            this.status = "EXPIRED";
            this.returnDate = this.dueDate; // 만료일을 반납일로 설정
        }
    }

    // 대출 연장
    public void extendLoan() {
        if (canExtend()) {
            this.extendNumber++;
            this.dueDate = this.dueDate.plusDays(7); // 7일 연장
        }
    }

//    // 조기 반납
//    public void returnEarly() {
//        this.status = "RETURNED";
//        this.returnDate = LocalDateTime.now();
//    }

    @PrePersist
    protected void onCreate() {
        if (borrowDate == null) {
            borrowDate = LocalDateTime.now();
        }
        // 대출 기간 설정 (책의 대출 기간 14일)
        if (dueDate == null && book != null) {
            int loanDays = 14;
            dueDate = borrowDate.plusDays(loanDays);
        }
    }
}
