package com.database.booktrace.Controller;

import com.database.booktrace.Dto.Response.BookSearchResponseDto;
import com.database.booktrace.Service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/book")
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
public class BookController {

    private final BookService bookService;

    @GetMapping("/search")
    public ResponseEntity<?> getBookDetail(@RequestParam String title) {
        log.info("도서 상세 검색 요청 - title: {}", title);
        BookSearchResponseDto response = bookService.searchBook(title);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }
}