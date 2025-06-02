package com.database.booktrace.Repository;

import com.database.booktrace.Domain.Book;
import com.database.booktrace.Domain.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface LoanRepository extends JpaRepository<Loan,Long> {

    //사용자가 대출한 도서id 목록 조회
    @Query(value="SELECT l.BOOK_ID FROM LOANS l WHERE l.USER_ID=:userId  AND l.status = 'BORROWED'",nativeQuery = true)
    Set<Long> findBorrowedBookIdsByUserId(@Param("userId") Long id);

}
