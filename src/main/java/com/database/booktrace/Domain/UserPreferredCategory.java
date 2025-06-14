package com.database.booktrace.Domain;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserPreferredCategory {
    private Long userId;           // USER_ID (복합키)
    private BookCategory category; // CATEGORY (복합키)

    public UserPreferredCategory(Long userId, BookCategory category) {
        this.userId = userId;
        this.category = category;
    }
}
