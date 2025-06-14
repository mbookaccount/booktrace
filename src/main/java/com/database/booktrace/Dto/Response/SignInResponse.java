package com.database.booktrace.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignInResponse {
    private boolean success;
    private String message;
    private Long userId;  // null if failed
}