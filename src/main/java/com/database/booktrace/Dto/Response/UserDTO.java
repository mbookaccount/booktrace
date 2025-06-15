package com.database.booktrace.Dto.Response;

import com.database.booktrace.Domain.BookCategory;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Data
public class UserDTO {
    private String userName;
    private String loginId;
    private Integer mileage;
    private Set<BookCategory> interests;
} 