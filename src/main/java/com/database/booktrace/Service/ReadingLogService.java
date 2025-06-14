package com.database.booktrace.Service;


import com.database.booktrace.Dto.Response.ReadingLogResponse;
import com.database.booktrace.Repository.ReadingLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReadingLogService {

    @Autowired
    private ReadingLogRepository readingLogRepository;

    /**
     * 사용자의 독서 로그 목록을 조회합니다.
     * @param userId 사용자 ID
     * @return 독서 로그 응답 목록
     */
    @Transactional(readOnly = true)
    public List<ReadingLogResponse> getReadingLogsByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 null일 수 없습니다.");
        }

        return readingLogRepository.findByUserId(userId);
    }
} 