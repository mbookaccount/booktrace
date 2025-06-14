package com.database.booktrace.Domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BookCategory {
//    HEALTH("건강"),
//    ECONOMY("경제"),
//    SCIENCE("과학"),
//    TECHNOLOGY("기술"),
//    LITERATURE("문학"),
//    NOVEL("소설"),
//    HISTORY("역사"),
//    SELF_DEVELOPMENT("자기계발");
    HEALTH("HEALTH", "건강"),
    ECONOMY("ECONOMY", "경제"),
    SCIENCE("SCIENCE", "과학"),
    TECHNOLOGY("TECHNOLOGY", "기술"),
    LITERATURE("LITERATURE", "문학"),
    NOVEL("NOVEL", "소설"),
    HISTORY("HISTORY", "역사"),
    SELF_DEVELOPMENT("SELF_DEVELOPMENT", "자기계발");
    private final String value;
    private final String displayName;

    BookCategory(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static BookCategory fromString(String value) {
        for (BookCategory category : BookCategory.values()) {
            if (category.value.equals(value)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Invalid category: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}