package com.database.booktrace.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CheckLoginIdRequest {
    @NotBlank(message = "로그인 ID를 입력해주세요.")
    private String loginId;

    public CheckLoginIdRequest(String loginId) {
        this.loginId = loginId;
    }
}
