package com.database.booktrace.Service;

import com.database.booktrace.Domain.Book;
import com.database.booktrace.Domain.BookCategory;
import com.database.booktrace.Domain.User;
import com.database.booktrace.Dto.Response.RecommendedBookDTO;
import com.database.booktrace.Repository.BookRepository;
import com.database.booktrace.Repository.LoanRepository;
import com.database.booktrace.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RecommendationService {

    private final UserRepository userRepository;
    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;

    // 사용자별 선호 카테고리 기반 추천 도서 조회
    public List<RecommendedBookDTO> getRecommendedBooks(Long id, int limit){

        // 사용자 정보 및 선호 카테고리 조회
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 개인 선호 카테고리 조회
        Set<BookCategory> preferredCategories=user.getPreferredCategories();

        // 개인 선호 카테고리 없을 경우
        if (preferredCategories.isEmpty()) {
            log.info("선호 카테고리가 없는 사용자 {}에게 인기 도서 추천", id);
            return getPopularBooks(limit);
        }

        // 사용자가 이미 대출한 도서 ID 목록 조회
        Set<Long> borrowedBookIds = loanRepository.findBorrowedBookIdsByUserId(id);
        List<RecommendedBookDTO> recommendations = new ArrayList<>();

        // 각 선호 카테고리별로 추천 도서 조회
        for (BookCategory category : preferredCategories) {
            List<RecommendedBookDTO> categoryBooks = getBooksByCategory(
                    category, id, limit / preferredCategories.size() + 1
            );
            recommendations.addAll(categoryBooks);
        }

        // 중복 제거 및 정렬
        Map<Long, RecommendedBookDTO> uniqueBooks = recommendations.stream()
                .collect(Collectors.toMap(
                        RecommendedBookDTO::getBookId,
                        book -> book,
                        (existing, replacement) -> existing // 중복 시 기존 것 유지
                ));

        List<RecommendedBookDTO> result = new ArrayList<>(uniqueBooks.values());

        // 추천 점수로 정렬 (가용한 책 우선, 예약 대기 적은 순)
        result.sort((a, b) -> {
            // 대출 가능한 책 우선
            if (!a.getIsAvailable().equals(b.getIsAvailable())) {
                return b.getIsAvailable().compareTo(a.getIsAvailable());
            }
            // 예약 대기가 적은 순
            return a.getReservationCount().compareTo(b.getReservationCount());
        });

        // 개수 제한
        return result.stream().limit(limit).collect(Collectors.toList());
    }


    // 카테고리별 추천 도서 조회

    private List<RecommendedBookDTO> getBooksByCategory(BookCategory category,
                                                        Long id,
                                                        int limit) {
        return bookRepository.findRecommendedBooksByCategory(category.name(), id, limit)
                .stream()
                .map(this::convertToRecommendedDTO)
                .collect(Collectors.toList());
    }


    // 선호 카테고리가 없는 경우 -> 인기 도서 추천
    public List<RecommendedBookDTO> getPopularBooks(int limit) {
        return bookRepository.findPopularBooks(limit)
                .stream()
                .map(book -> {
                    RecommendedBookDTO dto = convertToRecommendedDTO(book);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    //Book 엔티티 -> RecommendedBookDTO
    private RecommendedBookDTO convertToRecommendedDTO(Book book) {
        return RecommendedBookDTO.builder()
                .bookId(book.getBookId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .publisher(book.getPublisher())
                .category(book.getCategory())
                .coverImage(book.getCoverImage())
                .description(book.getDescription())
                .availableAmount(book.getAvailableAmount())
                .isAvailable(book.canBorrow())
                .reservationCount(book.getReservationCount())
                .build();
    }
}
