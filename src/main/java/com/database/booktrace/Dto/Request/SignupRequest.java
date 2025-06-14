package com.database.booktrace.Dto.Request;

import com.database.booktrace.Domain.BookCategory;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class SignupRequest {
    @NotBlank(message="이름을 입력해주세요.")
    @Size(min = 2, max = 20, message = "사용자명은 2~20자 사이여야 합니다.")
    @JsonProperty("name")
    private String userName;

    @NotBlank(message="아이디를 입력해주세요.")
    @Size(min = 2, max = 20, message = "아이디는 2~20자 사이여야 합니다.")
    @JsonProperty("id")
    private String userId;

    @NotBlank(message="비밀번호를 입력해주세요")
    @JsonProperty("password")
    private String password;

    @NotBlank(message="비밀번호 확인을 입력해주세요.")
    @JsonProperty("confirmPassword")
    private String confirmPassword;

    // String 리스트로 받아서 BookCategory로 변환하도록 수정
    @JsonProperty("preferredCategories")
    private Set<BookCategory> preferredCategories;
}
