package com.database.booktrace.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "loans")
@Getter
@Setter
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "loan_seq")
    @SequenceGenerator(name = "loan_seq", sequenceName = "loan_seq", allocationSize = 1)
    @Column(name = "loan_id")
    private Long loanId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(name = "borrow_date", nullable = false)
    private LocalDateTime borrowDate;

    @Column(name = "return_date", nullable = false)
    private LocalDateTime returnDate;

    @Column(name = "extend_number")
    private Integer extendNumber = 0;

    @Column(name = "status", nullable = false)
    private String status;
} 