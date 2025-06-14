package com.database.booktrace.Controller;

import com.database.booktrace.Domain.Exceptions.InvalidInputException;
import com.database.booktrace.Domain.Exceptions.UserNotFoundException;
import com.database.booktrace.Domain.User;
import com.database.booktrace.Dto.Request.SigninRequest;
import com.database.booktrace.Dto.Response.SignInResponse;
import com.database.booktrace.Service.SignInService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
public class SignInController {
;
    private final SignInService signInService;

    @PostMapping("/api/user/login")
    public ResponseEntity<SignInResponse> login(@RequestBody SigninRequest request,
                                                HttpSession session) {
        log.info("로그인 요청 - loginId: {}", request.getUserId());
        SignInResponse response = signInService.login(request.getUserId(), request.getPassword());
        if (response.isSuccess()) {
            session.setAttribute("userId", response.getUserId());  //세션에 userId 저장
            log.info("세션 생성 - sessionId: {}, userId: {}", session.getId(), response.getUserId());

            return ResponseEntity.ok(new SignInResponse(
                    true,
                    "로그인 성공",
                    null
            ));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
