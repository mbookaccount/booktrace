package com.database.booktrace.Domain;

public enum BookCategory {
    NOVEL("소설"),
    ESSAY("에세이"),
    SELF_DEVELOPMENT("자기계발"),
    BUSINESS("경영/경제"),
    SCIENCE("과학/기술"),
    HISTORY("역사"),
    PHILOSOPHY("철학"),
    ART("예술"),
    HEALTH("건강"),
    HOBBY("취미/여행"),
    EDUCATION("교육"),
    COMPUTER("컴퓨터/IT");

    private final String displayName;

    BookCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}