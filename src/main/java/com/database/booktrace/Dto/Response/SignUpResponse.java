package com.database.booktrace.Dto.Response;

import com.database.booktrace.Domain.BookCategory;

import java.time.LocalDateTime;
import java.util.Set;

public class SignUpResponse {
    public Long userId;

    public SignUpResponse(long l, String trim, String trim1, Set<BookCategory> preferredCategories, LocalDateTime now, String message) {
    }

    public SignUpResponse(boolean b, String message, Long userId) {
    }
}
