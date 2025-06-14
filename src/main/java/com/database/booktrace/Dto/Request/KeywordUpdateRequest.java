package com.database.booktrace.Dto.Request;
import lombok.Data;
import java.util.List;
import java.util.Set;

@Data
public class KeywordUpdateRequest {
    private Set<String> keywords;
}
