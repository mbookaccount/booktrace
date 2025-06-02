package com.database.booktrace.service;

import com.database.booktrace.dto.PopularBookDTO;
import com.database.booktrace.repository.PopularRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PopularService {

    @Autowired
    private PopularRepository popularRepository;

    public List<PopularBookDTO> getWeeklyPopularBooks() {
        return popularRepository.findWeeklyPopularBooks();
    }

    public List<PopularBookDTO> getMonthlyPopularBooks() {
        return popularRepository.findMonthlyPopularBooks();
    }
} 