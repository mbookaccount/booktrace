package com.database.booktrace.Domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "BOOKS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SequenceGenerator(
        name = "BOOK_SEQ_GEN",
        sequenceName = "BOOK_SEQ",
        initialValue = 1,
        allocationSize = 1
)
public class Book extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BOOK_SEQ_GEN")
    @Column(name = "BOOK_ID")
    private Long bookId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LIBRARY_ID", nullable = false)
    private Library library;

    @Column(name = "TITLE", nullable = false, length = 200)
    private String title;

    @Column(name = "AUTHOR", nullable = false, length = 100)
    private String author;

    @Column(name = "PUBLISHER", length = 100)
    private String publisher;

    @Column(name = "PUBLISHED_DATE")
    private LocalDateTime publishedDate;

//    @Column(name = "ISBN", unique = true, length = 20)
//    private String isbn;

    @Enumerated(EnumType.STRING)
    @Column(name = "CATEGORY", nullable = false, length = 30)
    private BookCategory category;

    @Column(name = "AVAILABLE_AMOUNT", nullable = false)
    @Builder.Default
    private Integer availableAmount = 1;

    @Lob
    @Column(name = "DESCRIPTION")
    private String description;  // Oracle CLOB

    @Column(name = "COVER_IMAGE", length = 500)
    private String coverImage;

//    @Column(name = "EBOOK_URL", length = 500)
//    private String ebookUrl;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Loan> loans;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reservation> reservations;

    // 비즈니스 로직

    // 빌릴 수 있는지 여부
    public boolean canBorrow() {
        return availableAmount > 0;
    }

//    // 책 빌리기 -> 트리거 이용 예정
//    public void borrow() {
//        if (canBorrow()) {
//            this.availableAmount--;
//        }
//    }

//    // 책 반납 -> 트리거 이용 예정
//    public void returnBook() {
//        this.availableAmount++;
//    }

    // 예약 대기자 수 계산
    public int getReservationCount() {
        if (reservations == null) return 0;
        return (int) reservations.stream()
                .filter(reservation -> "ACTIVE".equals(reservation.getStatus()))
                .count();
    }
}