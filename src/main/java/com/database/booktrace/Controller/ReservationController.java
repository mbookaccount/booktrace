package com.database.booktrace.Controller;

import com.database.booktrace.Service.ReservationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reserve")
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/{bookId}")
    public ResponseEntity<?> reserveBook(@PathVariable Long bookId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "로그인이 필요합니다."
            ));
        }

        Map<String, Object> result = reservationService.reserveBook(userId, bookId);

        int status = (Integer) result.get("p_result");
        String message = (String) result.get("p_message");

        return ResponseEntity.ok(Map.of(
                "success", status == 1,
                "message", message
        ));
    }
}