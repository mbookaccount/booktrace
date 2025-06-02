package com.database.booktrace.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UserDTO {
    private String loginId;
    private String userName;
    private Integer mileage;
    private Set<String> interests;
} 