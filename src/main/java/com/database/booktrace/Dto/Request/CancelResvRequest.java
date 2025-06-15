package com.database.booktrace.Dto.Request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CancelResvRequest {
    @JsonProperty(value="reservationId")
    private Long reservationId;
}
