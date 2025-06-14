package com.database.booktrace.Dto.Response;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class CheckLoginIdResponse {
    private boolean available;
    private String loginId;

    public CheckLoginIdResponse(boolean available, String loginId) {
        this.available = available;
        this.loginId = loginId;
    }
}
