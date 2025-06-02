package com.database.booktrace.Controller;


import com.database.booktrace.Dto.Response.RecommendedBookDTO;
import com.database.booktrace.Service.RecommendationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Slf4j
public class RecommendationController {

    private final RecommendationService recommendationService;

    // 추천도서 API
    @GetMapping("/personalized")
    public ResponseEntity<?> getRecommendations(
            @RequestParam(defaultValue="4") int limit,
            HttpSession session
    ){
        try{
            //세션에서 USER_ID 가져오기
            Long userId= (Long) session.getAttribute("userId");
            if(userId==null){
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                        .body();
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        Map.of("error","Unauthorized",
                                "message","로그인이 필요합니다.")
                );
            }

            List<RecommendedBookDTO> recommendations=
                    recommendationService.getRecommendedBooks(userId,limit);

            return ResponseEntity.ok(recommendations);

        }catch(Exception e){
            log.error("개인화 추천 도서 조회 중 오류 발생", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).
//                    body(createErrorResponse());
            return ResponseEntity.internalServerError().body(
                    Map.of("error","Internal Server Error",
                            "message","서버 오류가 발생했습니다")
            );
        }

    }
    @GetMapping("/session-info")
    public ResponseEntity<?> getSessionInfo(HttpSession session) {
        Map<String, Object> sessionInfo = new HashMap<>();
        sessionInfo.put("sessionId", session.getId());
        sessionInfo.put("userId", session.getAttribute("userId"));
        sessionInfo.put("isNew", session.isNew());
        sessionInfo.put("creationTime", session.getCreationTime());

        return ResponseEntity.ok(sessionInfo);
    }


}
