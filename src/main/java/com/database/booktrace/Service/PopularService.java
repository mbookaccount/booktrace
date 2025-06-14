package com.database.booktrace.Service;

import com.database.booktrace.Dto.Response.PopularBookDTO;
import com.database.booktrace.Repository.PopularRepository;
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