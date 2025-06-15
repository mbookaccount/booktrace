package com.database.booktrace.Service;

import com.database.booktrace.Dto.Response.RecommendedBookDTO;
import com.database.booktrace.Repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;

    @Transactional(readOnly = true)
    public List<RecommendedBookDTO> getRecommendationsByInterests(Long userId, int limit) {
        return recommendationRepository.getRecommendedBooks(userId, limit);
    }
}
