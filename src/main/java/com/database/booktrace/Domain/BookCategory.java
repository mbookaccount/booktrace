package com.database.booktrace.Domain;

public enum BookCategory {
    HEALTH("건강"),
    ECONOMY("경제"),
    SCIENCE("과학"),
    TECHNOLOGY("기술"),
    LITERATURE("문학"),
    NOVEL("소설"),
    HISTORY("역사"),
    SELF_DEVELOPMENT("자기계발");

    private final String displayName;

    BookCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}