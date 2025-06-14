package com.database.booktrace.Service;

import com.database.booktrace.Domain.Exceptions.InvalidInputException;
import com.database.booktrace.Domain.Exceptions.UserNotFoundException;
import com.database.booktrace.Domain.User;
import com.database.booktrace.Dto.Response.SignInResponse;
import com.database.booktrace.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.mindrot.jbcrypt.BCrypt;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SignInService {
    private final UserRepository userRepository;

    public SignInResponse login(String loginId, String password) {
        if (loginId == null || password == null || loginId.isBlank() || password.isBlank()) {
            return new SignInResponse(false, "로그인 ID와 비밀번호를 모두 입력해주세요.", null);
        }

        Long userId = userRepository.loginUser(loginId, password);
        if (userId != null) {
            return new SignInResponse(true, "로그인 성공", userId);
        } else {
            return new SignInResponse(false, "로그인 ID 또는 비밀번호가 올바르지 않습니다.", null);
        }
    }
}
