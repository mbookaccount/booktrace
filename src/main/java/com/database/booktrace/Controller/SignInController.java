//package com.database.booktrace.Controller;
//
//import com.database.booktrace.Domain.Exceptions.InvalidInputException;
//import com.database.booktrace.Domain.Exceptions.UserNotFoundException;
//import com.database.booktrace.Domain.User;
//import com.database.booktrace.Dto.Request.SigninRequest;
//import com.database.booktrace.Service.SignInService;
//import jakarta.servlet.http.HttpSession;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.validation.BindingResult;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@RestController
//@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:3000")
//@Slf4j
//public class SignInController {
//;
//    private final SignInService signInService;
//
//    @PostMapping("/api/user/login")
//    public ResponseEntity<?> signIn(@Valid @RequestBody SigninRequest request,
//                                    BindingResult bindingResult,
//                                    HttpSession session){
//        if (bindingResult.hasErrors()) {
//            return handleValidationErrors(bindingResult);
//        }
//        try{
//            User user=signInService.login(request.getUserId(),request.getPassword());
//            // 세션에 사용자 정보 저장
//            session.setAttribute("userId", user.getUserId());
//            session.setAttribute("userName", user.getUserName());
//
//            log.info("로그인 성공: 세션 ID = {}", session.getId());
//            return ResponseEntity.ok(createLoginSuccessResponse(user, session.getId()));
//
//        }catch(UserNotFoundException e){
//            log.warn("로그인 실패 - 사용자 없음: {}", e.getMessage());
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(createErrorResponse("USER_NOT_FOUND", e.getMessage()));
//        }catch(InvalidInputException e){
//            log.warn("로그인 실패 - 잘못된 비밀번호 입력: {}", e.getMessage());
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(createErrorResponse("INVALID_CREDENTIALS", e.getMessage()));
//        }catch (Exception e) {
//            log.error("로그인 처리 중 예상치 못한 오류: {}", e.getMessage(), e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(createErrorResponse("SERVER_ERROR", "서버 내부 오류가 발생했습니다."));
//        }
//    }
//
//    // 유효성 검증 오류 처리
//    private ResponseEntity<Map<String, Object>> handleValidationErrors(BindingResult bindingResult) {
//        Map<String, String> errorMap = new HashMap<>();
//        bindingResult.getFieldErrors().forEach(error -> {
//            String fieldName = error.getField();
//            String errorMessage = error.getDefaultMessage();
//            errorMap.put(fieldName, errorMessage);
//            log.warn("로그인 유효성 검증 실패: {} - {}", fieldName, errorMessage);
//        });
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("success", false);
//        response.put("errorCode", "VALIDATION_ERROR");
//        response.put("message", "입력값 검증에 실패했습니다.");
//        response.put("errors", errorMap);
//
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//    }
//
//    private Map<String, Object> createLoginSuccessResponse(User user, String sessionId) {
//        Map<String, Object> response = new HashMap<>();
//        response.put("success", true);
//        response.put("message", "로그인 성공");
//        response.put("sessionId", sessionId);
//
//        Map<String, Object> userInfo = new HashMap<>();
//        userInfo.put("id", user.getUserId());
//        userInfo.put("userId", user.getUserId());
//        response.put("user", userInfo);
//        return response;
//    }
//
//    private Map<String, Object> createErrorResponse(String errorCode, String message) {
//        Map<String, Object> response = new HashMap<>();
//        response.put("success", false);
//        response.put("errorCode", errorCode);
//        response.put("message", message);
//        return response;
//    }
//}
