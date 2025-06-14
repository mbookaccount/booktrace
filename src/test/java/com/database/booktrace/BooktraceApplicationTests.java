package com.database.booktrace;

import com.database.booktrace.Domain.BookCategory;
import com.database.booktrace.Domain.User;
import com.database.booktrace.Domain.Book;
import com.database.booktrace.Dto.Response.PopularBookDTO;
import com.database.booktrace.Dto.Response.RecommendedBookDTO;
import com.database.booktrace.Repository.BookRepository;
import com.database.booktrace.Repository.PopularRepository;
import com.database.booktrace.Repository.UserRepository;
import com.database.booktrace.Service.RecommendationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SpringBootTest
@Transactional
class BooktraceApplicationTests {
	@Autowired
	private RecommendationService recommendationService;

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private BookRepository bookRepository;
	@Autowired
	private PopularRepository popularRepository;
	@Test
	void 추천_시스템_테스트() {
		System.out.println("=== 추천 시스템 테스트 시작 ===");

		// testuser 찾기
		User user = userRepository.findByUsername("testuser").get();

		if (user == null) {
			System.out.println("testuser를 찾을 수 없습니다!");
			return;
		}

		System.out.println(" 사용자 찾음:");
		System.out.println("  - ID: " + user.getUserId());
		System.out.println("  - 이름: " + user.getUserName());
		System.out.println("  - 로그인ID: " + user.getLoginId());
		System.out.println("  - 선호 카테고리: " + user.getPreferredCategories());

		// 추천 받기
		System.out.println("\n추천 도서 조회 중...");
		List<RecommendedBookDTO> result = recommendationService.getRecommendedBooks(user.getId(), 4);

		System.out.println("추천 완료! 총 " + result.size() + "권");
		System.out.println("\n===추천 결과 ===");

		for (int i = 0; i < result.size(); i++) {
			RecommendedBookDTO book = result.get(i);
			System.out.println((i+1) + ". " + book.getTitle() +
					" - " + book.getAuthor() +
					" (" + book.getCategory() + ") " +
					"| 대출가능: " + book.getIsAvailable() +
					" | 남은수량: " + book.getAvailableAmount() + "권");
		}

		System.out.println("\n테스트 완료!");

		// 검증
		assert result.size() > 0 : "추천 도서가 없습니다!";
		System.out.println("기본 검증 통과");
	}
	@Test
	void Repository_직접_테스트() {
		System.out.println("=== Repository 직접 테스트 ===");

		// 1. COMPUTER 카테고리로 직접 조회
		List<Book> computerBooks = bookRepository.findRecommendedBooksByCategory("COMPUTER", 1L, 5);
		System.out.println("COMPUTER 카테고리 조회 결과: " + computerBooks.size() + "권");

		// 2. NOVEL 카테고리로 직접 조회
		List<Book> novelBooks = bookRepository.findRecommendedBooksByCategory("NOVEL", 1L, 5);
		System.out.println("NOVEL 카테고리 조회 결과: " + novelBooks.size() + "권");

		// 3. 인기 도서 조회
		List<PopularBookDTO> monthlyPopularBooks = popularRepository.findMonthlyPopularBooks();
//		List<Book> popularBooks = bookRepository.findPopularBooks(5);
		System.out.println("인기 도서 조회 결과: " + monthlyPopularBooks.size() + "권");

		// 4. 결과 출력
		computerBooks.forEach(book ->
				System.out.println("COMPUTER: " + book.getTitle())
		);

		novelBooks.forEach(book ->
				System.out.println("NOVEL: " + book.getTitle())
		);

		monthlyPopularBooks.forEach(book ->
				System.out.println("인기: " + book.getTitle())
		);
	}

	@Test
	void 서비스_로직_디버깅() {
		System.out.println("=== 서비스 로직 디버깅 ===");

		User user = userRepository.findByUsername("testuser").get();
		System.out.println("사용자 ID: " + user.getUserId());
		System.out.println("선호 카테고리: " + user.getPreferredCategories());

		// 서비스의 각 단계별로 확인
		Set<BookCategory> preferredCategories = user.getPreferredCategories();
		System.out.println("선호 카테고리 개수: " + preferredCategories.size());
		System.out.println("선호 카테고리가 비어있는가? " + preferredCategories.isEmpty());

		if (preferredCategories.isEmpty()) {
			System.out.println("선호 카테고리가 비어있어서 인기 도서로 이동");
			List<RecommendedBookDTO> popularBooks = recommendationService.getPopularBooks(4);
			System.out.println("인기 도서 추천: " + popularBooks.size() + "권");
			return;
		}

		// 각 카테고리별로 따로 확인
		List<RecommendedBookDTO> allRecommendations = new ArrayList<>();

		for (BookCategory category : preferredCategories) {
			System.out.println("\n--- " + category + " 카테고리 처리 ---");
			System.out.println("카테고리명: " + category.name());

			// getBooksByCategory 메소드 직접 호출 (private이면 public으로 임시 변경)
			// 또는 recommendationService에 디버깅 로그 추가

			int limitPerCategory = 4 / preferredCategories.size() + 1;
			System.out.println("카테고리당 제한: " + limitPerCategory);
		}

		// 최종 추천 결과
		List<RecommendedBookDTO> finalResult = recommendationService.getRecommendedBooks(user.getId(), 4);
		System.out.println("\n=== 최종 결과 ===");
		System.out.println("추천 도서 개수: " + finalResult.size());

		finalResult.forEach(book ->
				System.out.println("- " + book.getTitle() + " (" + book.getCategory() + ")")
		);
	}

}
