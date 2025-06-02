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
@Table(name = "RESERVATIONS")
@SequenceGenerator(
        name = "resv_seq_gen",
        sequenceName = "seq_resv_id",
        allocationSize = 1
)
public class Reservation extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "resv_seq_gen")
    @Column(name = "RESV_ID")  // ERD에서 log_id로 되어있으니 맞춤
    private Long resvId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BOOK_ID", nullable = false)
    private Book book;

    @Column(name = "RESV_DATE") //예약 날짜
    private LocalDateTime resvDate;

    @Column(name = "STATUS", length = 20)
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, COMPLETED, CANCELLED

    // 예약 취소
    public void cancel() {
        this.status = "CANCELLED";
    }

    // 예약 완료 (대출로 전환됨)
    public void complete() {
        this.status = "COMPLETED";
    }

    // 예약이 활성 상태인지 확인
    public boolean isActive() {
        return "ACTIVE".equals(this.status);
    }

    @PrePersist
    protected void onCreate() {
        if (resvDate == null) {
            resvDate = LocalDateTime.now();
        }
    }
}
