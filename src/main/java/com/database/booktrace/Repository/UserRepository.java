package com.database.booktrace.Repository;


import com.database.booktrace.Domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    @Query(value = "SELECT COUNT(*) FROM USERS WHERE LOGIN_ID = ?1", nativeQuery = true)
    int countByUserId(String userId);

    @Query(value = "SELECT COUNT(*) FROM USERS WHERE USER_NAME = ?1", nativeQuery = true)
    int countByUserName(String userName);

    @Query(value= """
            SELECT CASE
                WHEN EXISTS (SELECT 1 FROM USERS WHERE LOGIN_ID=?1)
                THEN 1 ELSE 0
            END FROM DUAL
            """,nativeQuery = true)
    int existsByUserId(String userId);

    @Query(value= """
            SELECT CASE
                WHEN EXISTS (SELECT 1 FROM USERS WHERE USER_NAME=?1)
                THEN 1 ELSE 0
            END FROM DUAL
            """,nativeQuery = true)
    int existsByUserName(String userName);


    Optional<User> findByUserId(String LoginId);
}
