package com.database.booktrace.dto.request;
import lombok.Data;
import java.util.List;
import java.util.Set;

@Data
public class KeywordUpdateRequest {
    private Set<String> keywords;
}
