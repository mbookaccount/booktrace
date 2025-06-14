package com.database.booktrace.Service;

import com.database.booktrace.Repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public Map<String, Object> reserveBook(Long userId, Long bookId) {
        return reservationRepository.reserveBook(userId, bookId);
    }
}
