package com.database.booktrace.Service;


import com.database.booktrace.Domain.BookCategory;
import com.database.booktrace.Domain.User;
import com.database.booktrace.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignUpService {

    private final UserRepository userRepository;

    public Long createUser(
            String userName,
            String userId,
            String password,
            String confirmPassword,
            Set<BookCategory> preferredCategories
    ){
        validatePassword(password,confirmPassword);
        validateDuplicateUser(userId,userName);

        // 비밀번호 암호화 (BCrypt 직접 사용)
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        User user=new User();
        user.setUserId(userId);
        user.setUserName(userName);
        user.setPassword(hashedPassword);
        user.setPreferredCategories(preferredCategories);

        User savedUser= userRepository.save(user);

        return savedUser.getId();
    }

    /*
    *  public longUser(String id,String password,String confirmPassword){
    *
    *    Users user=usersRepository.findByUserId(id)
    *               .ElseThrow(new UserNotFoundException("회원을 찾지 못했습니다."));
    *
    *
    *   }
    *
    * */
    // 로그인 시 비밀번호 검증용 메서드
    public boolean verifyPassword(String rawPassword, String hashedPassword) {
        return BCrypt.checkpw(rawPassword, hashedPassword);
    }

    private void validatePassword(String password, String confirmPassword){
        if(!password.equals(confirmPassword)){
            throw new IllegalStateException("비밀번호가 일치하지 않습니다.");
        }
    }

    private void validateDuplicateUser(String userId, String name){
        if (userRepository.existsByUserId(userId)>0) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        if (userRepository.existsByUserName(name)>0) {
            throw new IllegalArgumentException("이미 존재하는 이름입니다.");
        }
    }
}
