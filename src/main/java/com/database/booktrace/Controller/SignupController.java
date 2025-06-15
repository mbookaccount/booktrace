package com.database.booktrace.Controller;


import com.database.booktrace.Dto.Request.CheckLoginIdRequest;
import com.database.booktrace.Dto.Request.SignupRequest;
import com.database.booktrace.Dto.Response.CheckLoginIdResponse;
import com.database.booktrace.Dto.Response.SignUpResponse;
import com.database.booktrace.Service.SignUpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/user")
@Slf4j
public class SignupController {

    private final SignUpService signUpService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @Valid @RequestBody SignupRequest request,
            BindingResult bindingResult) {

        log.info("회원가입 요청 - 사용자명: {}, 로그인ID: {}", request.getUserName(), request.getUserId());

        // 유효성 검사 오류 확인
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().get(0).getDefaultMessage();
            log.warn("회원가입 유효성 검사 실패 - 메시지: {}", errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        Long response = signUpService.registerUser(request);
        return ResponseEntity.status(HttpStatus.OK).body(
                Map.of("success","true",
                        "message","회원가입이 성공적으로 완료되었습니다",
                                "userId",response)

        );
    }


    @PostMapping("/check-login-id")
    public ResponseEntity<CheckLoginIdResponse> checkLoginId(
            @Valid @RequestBody CheckLoginIdRequest request) {

        log.info("로그인 ID 중복확인 요청 - loginId: {}", request.getLoginId());

        CheckLoginIdResponse response = signUpService.checkLoginIdExists( request.getLoginId());
        return ResponseEntity.ok(response);
    }

}
