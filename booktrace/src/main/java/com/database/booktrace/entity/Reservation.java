package com.database.booktrace.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter
@Setter
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "resv_seq")
    @SequenceGenerator(name = "resv_seq", sequenceName = "resv_seq", allocationSize = 1)
    @Column(name = "resv_id")
    private Long resvId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Long bookId;

    @Column(name = "resv_date", nullable = false)
    private LocalDateTime resvDate;
} 