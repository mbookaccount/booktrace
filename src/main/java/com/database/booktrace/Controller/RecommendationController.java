package com.database.booktrace.Controller;

import com.database.booktrace.Dto.Response.RecommendedBookDTO;
import com.database.booktrace.Service.RecommendationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
public class RecommendationController {

    private final RecommendationService recommendationService;

    // 선호 카테고리 기반 추천 도서 API
    @GetMapping
    public ResponseEntity<?> getRecommendationsByInterests(
            @RequestParam(defaultValue = "10") int limit,
            HttpSession session
    ) {
        try {
            // 세션에서 USER_ID 가져오기
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        Map.of("error", "Unauthorized",
                                "message", "로그인이 필요합니다.")
                );
            }

            List<RecommendedBookDTO> recommendations =
                    recommendationService.getRecommendationsByInterests(userId, limit);

            return ResponseEntity.ok(recommendations);

        } catch (Exception e) {
            log.error("선호 카테고리 기반 추천 도서 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(
                    Map.of("error", "Internal Server Error",
                            "message", "서버 오류가 발생했습니다")
            );
        }
    }
}
