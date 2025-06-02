package com.database.booktrace.service;

import com.database.booktrace.dto.ReadingLogDTO;
import com.database.booktrace.entity.ReadingLog;
import com.database.booktrace.repository.ReadingLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReadingLogService {

    @Autowired
    private ReadingLogRepository readingLogRepository;

    public List<ReadingLogDTO> getReadingLogsByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        List<ReadingLog> logs = readingLogRepository.findByUserId(userId);
        return logs.stream()
                .map(log -> convertToDTO(log, logs.indexOf(log) + 1))
                .collect(Collectors.toList());
    }

    private ReadingLogDTO convertToDTO(ReadingLog log, int userLogNumber) {
        ReadingLogDTO dto = new ReadingLogDTO();
        dto.setUserLogNumber(userLogNumber);
        dto.setBookTitle(log.getBook().getTitle());
        dto.setBorrowDate(log.getBorrowDate());
        dto.setReturnDate(log.getReturnDate());
        dto.setMileage(log.getMileage());
        dto.setTotalMileage(log.getTotalMileage());
        return dto;
    }
} 