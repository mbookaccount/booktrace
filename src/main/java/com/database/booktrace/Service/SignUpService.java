package com.database.booktrace.Service;


import com.database.booktrace.Domain.BookCategory;
import com.database.booktrace.Domain.User;
import com.database.booktrace.Dto.Request.SignupRequest;
import com.database.booktrace.Dto.Response.CheckLoginIdResponse;
import com.database.booktrace.Dto.Response.SignUpResponse;
import com.database.booktrace.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.SqlParameter;  // 🆕 추가 필요
import org.springframework.jdbc.core.simple.SimpleJdbcCall;  // 🆕 추가 필요
import org.springframework.stereotype.Service;

import java.sql.Types;  // 🆕 추가 필요
import java.time.LocalDateTime;  // 🆕 추가 필요
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;  // 🆕 추가 필요

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignUpService {

    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    //로그인ID 중복 확인
    public CheckLoginIdResponse checkLoginIdExists(String loginId) {
        try {
            // 입력값 검증
            if (loginId == null || loginId.trim().isEmpty()) {
                throw new IllegalArgumentException("로그인 ID를 입력해주세요.");
            }
            // Oracle 함수 호출
            String sql = "SELECT CHECK_LOGIN_ID_EXISTS(?) FROM DUAL";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, loginId.trim());

            // 결과 처리
            boolean isAvailable = (count != null && count == 0);

            log.info("로그인 ID 중복확인 - ID: {}, 사용가능: {}", loginId, isAvailable);
            return new CheckLoginIdResponse(isAvailable, loginId);

        } catch (IllegalArgumentException e) {
            throw e;  // 유효성 검사 오류는 그대로 던지기
        } catch (Exception e) {
            log.error("로그인 ID 중복확인 오류 - ID: {}", loginId, e);
            throw new RuntimeException("로그인 ID 중복확인 중 오류가 발생했습니다.");
        }
    }

    //회원가입
    public Long registerUser(SignupRequest request) {
        String categoriesCsv = String.join(",", request.getPreferredCategories().stream()
                .map(BookCategory::getValue)
                .toList());

        Map<String, Object> result =userRepository.registerUser(
                request.getUserName(),
                request.getUserId(),
                request.getPassword(),
                request.getConfirmPassword(),
                categoriesCsv
        );

        int resultCode = (Integer) result.get("p_result");
        String message = (String) result.get("p_message");

        if (resultCode == 1) {
            Long userId = ((Number) result.get("p_user_id")).longValue();
            return userId;

        } else {
            throw new IllegalArgumentException(message);
        }
//        try {
//            // 선호 카테고리를 콤마로 구분된 문자열로 변환
//            String categoriesString = request.getPreferredCategories().stream()
//                    .map(BookCategory::getValue)
//                    .collect(Collectors.joining(","));
//
//
//            log.info("회원가입 시도 - 사용자명: {}, 로그인ID: {}, 카테고리: {}",
//                    request.getUserName(), request.getUserId(), categoriesString);
//
//            // Oracle 프로시저 호출
//            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
//                    .withProcedureName("REGISTER_USER")
//                    .declareParameters(
//                            new SqlParameter("p_user_name", Types.VARCHAR),
//                            new SqlParameter("p_login_id", Types.VARCHAR),
//                            new SqlParameter("p_password", Types.VARCHAR),
//                            new SqlParameter("p_password_confirm", Types.VARCHAR),
//                            new SqlParameter("p_preferred_categories", Types.VARCHAR),
//                            new SqlParameter("p_result", Types.NUMERIC),
//                            new SqlParameter("p_message", Types.VARCHAR),
//                            new SqlParameter("p_user_id", Types.NUMERIC)
//                    );
//
//            Map<String, Object> params = Map.of(
//                    "p_user_name", request.getUserName().trim(),
//                    "p_login_id", request.getUserId().trim(),
//                    "p_password", request.getPassword(),
//                    "p_password_confirm", request.getConfirmPassword(),
//                    "p_preferred_categories", categoriesString
//            );
//
//            Map<String, Object> result = jdbcCall.execute(params);
//
//            // 결과 처리
//            Number resultCode = (Number) result.get("p_result");
//            String message = (String) result.get("p_message");
//            Number userId = (Number) result.get("p_user_id");
//
//            if (resultCode != null && resultCode.intValue() == 1) {
//                // 성공 시 응답 생성
//                SignUpResponse signupResponse = new SignUpResponse(
//                        userId.longValue(),
//                        request.getUserName().trim(),
//                        request.getUserId().trim(),
//                        request.getPreferredCategories(),
//                        LocalDateTime.now(),
//                        message
//                );
//
//                log.info("회원가입 성공 - 사용자ID: {}, 로그인ID: {}", userId, request.getUserId());
//                return signupResponse;
//            } else {
//                log.warn("회원가입 실패 - 로그인ID: {}, 메시지: {}", request.getUserId(), message);
//                throw new RuntimeException(message != null ? message : "회원가입에 실패했습니다.");
//            }
//
//        } catch (IllegalArgumentException e) {
//            throw e;  // 유효성 검사 오류는 그대로 던지기
//        } catch (RuntimeException e) {
//            throw e;  // 비즈니스 로직 오류는 그대로 던지기
//        } catch (Exception e) {
//            log.error("회원가입 오류 - 로그인ID: {}", request.getUserId(), e);
//            throw new RuntimeException("회원가입 중 오류가 발생했습니다.");
//        }
    }


//
//    public Long createUser(
//            String userName,
//            String userId,
//            String password,
//            String confirmPassword,
//            Set<BookCategory> preferredCategories
//    ){
//        validatePassword(password,confirmPassword);
//        validateDuplicateUser(userId,userName);
//
//        // 비밀번호 암호화 (BCrypt 직접 사용)
//        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
//        User user=new User();
//        user.setUserId(userId);
//        user.setUserName(userName);
//        user.setPassword(hashedPassword);
//        user.setPreferredCategories(preferredCategories);
//
//        User savedUser= userRepository.save(user);
//
//        return savedUser.getId();
//    }
//
//    /*
//    *  public longUser(String id,String password,String confirmPassword){
//    *
//    *    Users user=usersRepository.findByUserId(id)
//    *               .ElseThrow(new UserNotFoundException("회원을 찾지 못했습니다."));
//    *
//    *
//    *   }
//    *
//    * */
//    // 로그인 시 비밀번호 검증용 메서드
//    public boolean verifyPassword(String rawPassword, String hashedPassword) {
//        return BCrypt.checkpw(rawPassword, hashedPassword);
//    }
//
//    private void validatePassword(String password, String confirmPassword){
//        if(!password.equals(confirmPassword)){
//            throw new IllegalStateException("비밀번호가 일치하지 않습니다.");
//        }
//    }
//
//    private void validateDuplicateUser(String userId, String name){
//        if (userRepository.existsByUserId(userId)>0) {
//            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
//        }
//
//        if (userRepository.existsByUserName(name)>0) {
//            throw new IllegalArgumentException("이미 존재하는 이름입니다.");
//        }
//    }
}
