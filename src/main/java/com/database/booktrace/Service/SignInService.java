package com.database.booktrace.Service;

import com.database.booktrace.Domain.Exceptions.InvalidInputException;
import com.database.booktrace.Domain.Exceptions.UserNotFoundException;
import com.database.booktrace.Domain.User;
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

//    public User login(String loginId, String password){
//        //사용자 조회
//        User user= userRepository.findByUserId(loginId)
//                .orElseThrow(()-> new UserNotFoundException("존재하지 않는 아이디입니다."));
//
//        //비밀번호 검증
//        if(!BCrypt.checkpw(password,user.getPassword())){
//            throw new InvalidInputException("비밀번호가 일치하지 않습니다.");
//        }
//
//        log.info("로그인 성공 : 사용자 ID {}",user.getUserId());
//
//        return user;
//    }

}
