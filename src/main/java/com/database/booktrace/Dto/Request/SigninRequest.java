package com.database.booktrace.Dto.Request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class SigninRequest {
    @NotBlank(message="아이디를 입력해주세요")
    @JsonProperty("userId")
    public String userId;

    @NotBlank(message="비밀번호를 입력해주세요")
    @JsonProperty("password")
    public String password;
}
