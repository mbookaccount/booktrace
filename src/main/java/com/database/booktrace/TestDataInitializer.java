//package com.database.booktrace;
//
//import com.database.booktrace.Domain.BookCategory;
//import com.database.booktrace.Domain.Book;
//import com.database.booktrace.Domain.Library;
//import com.database.booktrace.Domain.User;
//import com.database.booktrace.Repository.BookRepository;
//import com.database.booktrace.Repository.LibraryRepository;
//import com.database.booktrace.Repository.UserRepository;
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Set;
//
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class TestDataInitializer {
//
//    private final UserRepository userRepository;
//    private final LibraryRepository libraryRepository;
//    private final BookRepository bookRepository;
//
//    @PostConstruct
//    @Transactional
//    public void initTestData() {
//        // 이미 데이터가 있으면 스킵
//        if (userRepository.count() > 0) {
//            log.info("테스트 데이터가 이미 존재합니다.");
//            return;
//        }
//
//        log.info("테스트 데이터 생성 시작...");
//
//        Library library = new Library();
//        library.setName("테스트 전자도서관");
//        Library savedLibrary = libraryRepository.save(library);
//
//
//        // 테스트 사용자 생성
//        User testUser = new User();
//        testUser.setUserName("테스트 사용자");           // USER_NAME
//        testUser.setUserId("testuser");               // LOGIN_ID
//        testUser.setPassword("password123");          // PASSWORD
//        testUser.setMileage(1000L);                   // MILEAGE
//        testUser.setIsActive("Y");                    // IS_ACTIVE
//        testUser.setPreferredCategories(Set.of(BookCategory.TECHNOLOGY, BookCategory.NOVEL));
//        userRepository.save(testUser);
//        // 테스트 책들 생성
//        List<Book> testBooks = new ArrayList<>();
//
//        Book book1 = new Book();
//        book1.setLibrary(savedLibrary);
//        book1.setTitle("DB란 이런거임");
//        book1.setAuthor("심준호");
//        book1.setPublisher("숙명여대");
//        book1.setCategory(BookCategory.TECHNOLOGY);
//        book1.setAvailableAmount(3);
//        book1.setDescription("유명한 DB책");
//        book1.setCoverImage("https://example.com/db-book.jpg");
//        book1.setPublishedDate(LocalDateTime.of(2024, 3, 15, 0, 0));
//        testBooks.add(book1);
//
//
//        Book book2 = new Book();
//        book2.setLibrary(savedLibrary);
//        book2.setTitle("데이터베이스 설계와 구현");
//        book2.setAuthor("김데이터");
//        book2.setPublisher("테크북스");
//        book2.setCategory(BookCategory.TECHNOLOGY);
//        book2.setAvailableAmount(2);
//        book2.setDescription("실무에 바로 적용하는 DB 설계");
//        book2.setCoverImage("https://example.com/db-design.jpg");
//        book2.setPublishedDate(LocalDateTime.of(2023, 8, 20, 0, 0));
//        testBooks.add(book2);
//
//        Book book3 = new Book();
//        book3.setLibrary(savedLibrary);
//        book3.setTitle("소설 같은 인생");
//        book3.setAuthor("이작가");
//        book3.setPublisher("문학동네");
//        book3.setCategory(BookCategory.NOVEL);
//        book3.setAvailableAmount(5);
//        book3.setDescription("감동적인 인생 이야기");
//        book3.setCoverImage("https://example.com/novel-life.jpg");
//        book3.setPublishedDate(LocalDateTime.of(2024, 1, 10, 0, 0));
//        testBooks.add(book3);
//
//
//        Book book4 = new Book();
//        book4.setLibrary(savedLibrary);
//        book4.setTitle("나는 오늘도 성장한다");
//        book4.setAuthor("박성장");
//        book4.setPublisher("자기계발사");
//        book4.setCategory(BookCategory.SELF_DEVELOPMENT);
//        book4.setAvailableAmount(4);
//        book4.setDescription("매일 1% 성장하는 법");
//        book4.setCoverImage("https://example.com/growth.jpg");
//        book4.setPublishedDate(LocalDateTime.of(2024, 2, 5, 0, 0));
//        testBooks.add(book4);
//
//
//        Book book5 = new Book();
//        book5.setLibrary(savedLibrary);
//        book5.setTitle("경영학 원론");
//        book5.setAuthor("최경영");
//        book5.setPublisher("비즈니스북");
//        book5.setCategory(BookCategory.ECONOMY);
//        book5.setAvailableAmount(3);
//        book5.setDescription("기업 경영의 기초");
//        book5.setCoverImage("https://example.com/business.jpg");
//        book5.setPublishedDate(LocalDateTime.of(2023, 12, 1, 0, 0));
//        testBooks.add(book5);
//
//        Book book6 = new Book();
//        book6.setLibrary(savedLibrary);
//        book6.setTitle("양자역학의 세계");
//        book6.setAuthor("정과학");
//        book6.setPublisher("사이언스월드");
//        book6.setCategory(BookCategory.SCIENCE);
//        book6.setAvailableAmount(2);
//        book6.setDescription("신비로운 양자의 세계");
//        book6.setCoverImage("https://example.com/quantum.jpg");
//        book6.setPublishedDate(LocalDateTime.of(2024, 4, 12, 0, 0));
//        testBooks.add(book6);
//
//        Book book7 = new Book();
//        book7.setLibrary(savedLibrary);
//        book7.setTitle("조선왕조실록 이야기");
//        book7.setAuthor("한역사");
//        book7.setPublisher("역사출판");
//        book7.setCategory(BookCategory.HISTORY);
//        book7.setAvailableAmount(3);
//        book7.setDescription("조선 500년 역사");
//        book7.setCoverImage("https://example.com/joseon.jpg");
//        book7.setPublishedDate(LocalDateTime.of(2023, 11, 25, 0, 0));
//        testBooks.add(book7);
//
//        Book book8 = new Book();
//        book8.setLibrary(savedLibrary);
//        book8.setTitle("철학자의 생각법");
//        book8.setAuthor("서철학");
//        book8.setPublisher("지혜출판사");
//        book8.setCategory(BookCategory.HISTORY);
//        book8.setAvailableAmount(4);
//        book8.setDescription("위대한 철학자들의 사고방식");
//        book8.setCoverImage("https://example.com/philosophy.jpg");
//        book8.setPublishedDate(LocalDateTime.of(2024, 1, 30, 0, 0));
//        testBooks.add(book8);
//
//        Book book9 = new Book();
//        book9.setLibrary(savedLibrary);
//        book9.setTitle("모네의 그림 이야기");
//        book9.setAuthor("김예술");
//        book9.setPublisher("아트북스");
//        book9.setCategory(BookCategory.LITERATURE);
//        book9.setAvailableAmount(2);
//        book9.setDescription("인상파 거장 모네의 작품 세계");
//        book9.setCoverImage("https://example.com/monet.jpg");
//        book9.setPublishedDate(LocalDateTime.of(2024, 3, 8, 0, 0));
//        testBooks.add(book9);
//
//        Book book10 = new Book();
//        book10.setLibrary(savedLibrary);
//        book10.setTitle("건강한 삶을 위한 운동법");
//        book10.setAuthor("이건강");
//        book10.setPublisher("헬스라이프");
//        book10.setCategory(BookCategory.HEALTH);
//        book10.setAvailableAmount(5);
//        book10.setDescription("올바른 운동과 건강 관리");
//        book10.setCoverImage("https://example.com/health.jpg");
//        book10.setPublishedDate(LocalDateTime.of(2024, 2, 20, 0, 0));
//        testBooks.add(book10);
//
//        bookRepository.saveAll(testBooks);
//
//        log.info("테스트 데이터 생성 완료!");
//        log.info("- 도서관: 1개");
//        log.info("- 사용자: 1명");
//        log.info("- 도서: {}권", testBooks.size());
//    }
//}
