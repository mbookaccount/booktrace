package com.database.booktrace.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse {
    private Boolean status;
    private String message;
    private String error;

    public ErrorResponse(Boolean status, String message, String error) {
        this.status = status;
        this.message = message;
        this.error = error;
    }
} 