package com.database.booktrace.Controller;


import com.database.booktrace.Dto.Request.SignupRequest;
import com.database.booktrace.Service.SignUpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/user")
@Slf4j
public class SignupController {

    private final SignUpService signUpService;

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignupRequest request,
                                                 BindingResult bindingResult) {

        //유효성 검증 실패했을 때 처리
        if (bindingResult.hasErrors()) {
            return handleValidationErrors(bindingResult);
        }

        try {
            Long userId = signUpService.createUser(
                    request.getUserName(),
                    request.getUserId(),
                    request.getPassword(),
                    request.getConfirmPassword(),
                    request.getPreferredCategories()
            );

            log.info("회원가입 성공 : 사용자 ID {} ", userId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(createSuccessResponse("회원가입이 성공적으로 완료되었습니다",userId));

        } catch (IllegalStateException e) {
            log.error("회원가입 실패 - 비밀번호 불일치 : {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("PASSWORD_MISMATCH", e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.error("회원가입 실패 - 중복 데이터 : {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("DUPLICATE_DATA", e.getMessage()));
        }catch (Exception e) {
            log.error("회원가입 처리 중 예상치 못한 오류 : {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("SERVER_ERROR", "서버 내부 오류가 발생했습니다."));
        }
    }

    // 유효성 검증 오류 처리
    private ResponseEntity<Map<String, Object>> handleValidationErrors(BindingResult bindingResult) {
        Map<String, String> errorMap = new HashMap<>();
        bindingResult.getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            errorMap.put(fieldName, errorMessage);
            log.warn("회원가입 유효성 검증 실패: {} - {}", fieldName, errorMessage);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", "VALIDATION_ERROR");
        response.put("message", "입력값 검증에 실패했습니다.");
        response.put("errors", errorMap);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    private Map<String,Object> createSuccessResponse(String message, Long userId){
        Map<String,Object> response=new HashMap<>();
        response.put("success",true);
        response.put("message",message);
        response.put("userId",userId);
        return response;
    }

    private Map<String,Object> createErrorResponse(String errorCode, String message){
        Map<String,Object> response=new HashMap<>();
        response.put("success",false);
        response.put("errorCode", errorCode);
        response.put("message",message);
        return response;
    }

}
