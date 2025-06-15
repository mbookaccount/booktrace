package com.database.booktrace.Dto.Response;

import com.database.booktrace.Domain.BookCategory;
import lombok.Data;
import java.util.Set;

@Data
public class UserDTO {
    private String userName;
    private String loginId;
    private Integer mileage;
    private Set<BookCategory> interests;
} 