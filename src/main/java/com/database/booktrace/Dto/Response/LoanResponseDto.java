package com.database.booktrace.Dto.Response;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanResponseDTO {
    private Long loanId;
    private String result;
    private boolean success;
    private String message;

    public static LoanResponseDTO success(Long loanId, String message) {
        return LoanResponseDTO.builder()
                .loanId(loanId)
                .result("SUCCESS")
                .success(true)
                .message(message)
                .build();
    }

    public static LoanResponseDTO failure(String message) {
        return LoanResponseDTO.builder()
                .loanId(null)
                .result("FAILED")
                .success(false)
                .message(message)
                .build();
    }
}
