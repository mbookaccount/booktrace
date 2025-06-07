package com.database.booktrace.controller;

import com.database.booktrace.dto.response.ErrorResponse;
import com.database.booktrace.dto.PopularBookDTO;
import com.database.booktrace.service.PopularService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/popular")
public class PopularController {

    @Autowired
    private PopularService popularService;

    @GetMapping("/weekly")
    public ResponseEntity<?> getWeeklyPopularBooks() {
        try {
            List<PopularBookDTO> books = popularService.getWeeklyPopularBooks();
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(
                    false,
                    "다시 시도해주세요.",
                    e.getMessage()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/monthly")
    public ResponseEntity<?> getMonthlyPopularBooks() {
        try {
            List<PopularBookDTO> books = popularService.getMonthlyPopularBooks();
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(
                    false,
                    "다시 시도해주세요.",
                    e.getMessage()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
