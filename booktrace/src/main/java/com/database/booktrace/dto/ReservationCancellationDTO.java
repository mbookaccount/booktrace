package com.database.booktrace.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservationCancellationDTO {
    private Long reservationId;  // 취소할 예약 ID
} 