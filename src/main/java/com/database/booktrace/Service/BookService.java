package com.database.booktrace.Service;


import com.database.booktrace.Dto.Response.BookSearchResponseDto;
import com.database.booktrace.Repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private final BookRepository bookRepository;

    public BookSearchResponseDto searchBook(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("제목을 입력해주세요.");
        }
        return bookRepository.getBookDetailByTitle(title);
    }
}