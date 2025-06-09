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
    @JoinColumn(nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Book book;

    @Column(name = "resv_date", nullable = false)
    private LocalDateTime resvDate;
} 