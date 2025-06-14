package com.database.booktrace.Dto.Response;

import com.database.booktrace.Domain.BookCategory;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UserDTO {
    private String loginId;
    private String userName;
    private Integer mileage;
    private Set<BookCategory> interests;
} 