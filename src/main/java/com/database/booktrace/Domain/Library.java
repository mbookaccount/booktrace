// Library.java - LIBRARIES 테이블과 매핑
package com.database.booktrace.Domain;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class Library {
    private Long libraryId;        // LIBRARY_ID (PK)
    private String name;           // NAME
    private LocalDateTime createdAt;  // CREATED_AT
    private LocalDateTime updatedAt;  // UPDATED_AT

    public Library() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Library(String name) {
        this();
        this.name = name;
    }
}