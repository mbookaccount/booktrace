-- 예외 정의
CREATE OR REPLACE PACKAGE loan_exceptions AS
    max_extends_exceeded EXCEPTION;
    overdue_loan_exception EXCEPTION;
    already_reserved_exception EXCEPTION;
    not_found_exception EXCEPTION;
    
    PRAGMA EXCEPTION_INIT(max_extends_exceeded, -20001);
    PRAGMA EXCEPTION_INIT(overdue_loan_exception, -20002);
    PRAGMA EXCEPTION_INIT(already_reserved_exception, -20003);
    PRAGMA EXCEPTION_INIT(not_found_exception, -20004);
END loan_exceptions;
/

-- 대출 관련 패키지
CREATE OR REPLACE PACKAGE loan_package AS
    -- 커서 타입 정의
    TYPE loan_cursor IS REF CURSOR;
    
    -- 대출 정보를 담을 레코드 타입
    TYPE loan_record IS RECORD (
        loan_id loans.loan_id%TYPE,
        user_id loans.user_id%TYPE,
        book_id loans.book_id%TYPE,
        borrow_date loans.borrow_date%TYPE,
        return_date loans.return_date%TYPE,
        status loans.status%TYPE,
        extend_number loans.extend_number%TYPE,
        created_at loans.created_at%TYPE,
        updated_at loans.updated_at%TYPE,
        book_title books.title%TYPE,
        library_name libraries.name%TYPE
    );
    
    -- 사용자의 대출 목록 조회 (커서 사용)
    PROCEDURE get_user_loans(
        p_user_id IN loans.user_id%TYPE,
        p_result OUT loan_cursor
    );
    
    -- 특정 대출 정보 조회 (레코드 타입 사용)
    PROCEDURE get_loan_by_id(
        p_loan_id IN loans.loan_id%TYPE,
        p_result OUT loan_cursor
    );
    
    -- 대출 연장 (트랜잭션 처리)
    PROCEDURE extend_loan(
        p_loan_id IN loans.loan_id%TYPE,
        p_result OUT loan_cursor
    );
    
    -- 예약 취소 (트랜잭션 처리)
    PROCEDURE cancel_reservation(
        p_reservation_id IN loans.loan_id%TYPE
    );
    
    -- 도서 예약 여부 확인 (함수)
    FUNCTION has_reservation(
        p_book_id IN books.book_id%TYPE
    ) RETURN BOOLEAN;
    
    -- 대출 상태 업데이트 (트리거에서 사용)
    PROCEDURE update_loan_status(
        p_loan_id IN loans.loan_id%TYPE,
        p_status IN loans.status%TYPE
    );
END loan_package;
/

-- 패키지 본문
CREATE OR REPLACE PACKAGE BODY loan_package AS
    -- 사용자의 대출 목록 조회
    PROCEDURE get_user_loans(
        p_user_id IN loans.user_id%TYPE,
        p_result OUT loan_cursor
    ) IS
        v_loan loan_record;
    BEGIN
        OPEN p_result FOR
            SELECT l.*, b.title as book_title, lib.name as library_name
            FROM loans l
            JOIN books b ON l.book_id = b.book_id
            JOIN libraries lib ON b.library_id = lib.library_id
            WHERE l.user_id = p_user_id
            ORDER BY l.borrow_date DESC;
    END get_user_loans;
    
    -- 특정 대출 정보 조회
    PROCEDURE get_loan_by_id(
        p_loan_id IN loans.loan_id%TYPE,
        p_result OUT loan_cursor
    ) IS
        v_count NUMBER;
    BEGIN
        -- 대출 정보 존재 여부 확인
        SELECT COUNT(*) INTO v_count
        FROM loans
        WHERE loan_id = p_loan_id;
        
        IF v_count = 0 THEN
            RAISE loan_exceptions.not_found_exception;
        END IF;
        
        OPEN p_result FOR
            SELECT l.*, b.title as book_title, lib.name as library_name
            FROM loans l
            JOIN books b ON l.book_id = b.book_id
            JOIN libraries lib ON b.library_id = lib.library_id
            WHERE l.loan_id = p_loan_id;
    END get_loan_by_id;
    
    -- 대출 연장
    PROCEDURE extend_loan(
        p_loan_id IN loans.loan_id%TYPE,
        p_result OUT loan_cursor
    ) IS
        v_loan loans%ROWTYPE;
        v_book_id books.book_id%TYPE;
    BEGIN
        -- 대출 정보 조회
        SELECT * INTO v_loan
        FROM loans
        WHERE loan_id = p_loan_id;
        
        -- 연장 가능 여부 확인
        IF v_loan.extend_number >= 2 THEN
            RAISE loan_exceptions.max_extends_exceeded;
        END IF;
        
        IF v_loan.return_date < SYSDATE THEN
            RAISE loan_exceptions.overdue_loan_exception;
        END IF;
        
        -- 예약 여부 확인
        IF has_reservation(v_loan.book_id) THEN
            RAISE loan_exceptions.already_reserved_exception;
        END IF;
        
        -- 대출 연장 처리
        UPDATE loans
        SET return_date = return_date + 7,
            extend_number = extend_number + 1,
            updated_at = SYSDATE
        WHERE loan_id = p_loan_id;
        
        -- 업데이트된 대출 정보 조회
        OPEN p_result FOR
            SELECT l.*, b.title as book_title, lib.name as library_name
            FROM loans l
            JOIN books b ON l.book_id = b.book_id
            JOIN libraries lib ON b.library_id = lib.library_id
            WHERE l.loan_id = p_loan_id;
            
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE loan_exceptions.not_found_exception;
    END extend_loan;
    
    -- 예약 취소
    PROCEDURE cancel_reservation(
        p_reservation_id IN loans.loan_id%TYPE
    ) IS
        v_status loans.status%TYPE;
    BEGIN
        -- 예약 상태 확인
        SELECT status INTO v_status
        FROM loans
        WHERE loan_id = p_reservation_id;
        
        IF v_status != 'RESERVED' THEN
            RAISE loan_exceptions.not_found_exception;
        END IF;
        
        -- 예약 취소 처리
        DELETE FROM loans
        WHERE loan_id = p_reservation_id;
        
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE loan_exceptions.not_found_exception;
    END cancel_reservation;
    
    -- 도서 예약 여부 확인
    FUNCTION has_reservation(
        p_book_id IN books.book_id%TYPE
    ) RETURN BOOLEAN IS
        v_count NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v_count
        FROM loans
        WHERE book_id = p_book_id
        AND status = 'RESERVED';
        
        RETURN v_count > 0;
    END has_reservation;
    
    -- 대출 상태 업데이트
    PROCEDURE update_loan_status(
        p_loan_id IN loans.loan_id%TYPE,
        p_status IN loans.status%TYPE
    ) IS
    BEGIN
        UPDATE loans
        SET status = p_status,
            updated_at = SYSDATE
        WHERE loan_id = p_loan_id;
    END update_loan_status;
END loan_package;
/

-- 대출 상태 변경 트리거
CREATE OR REPLACE TRIGGER loan_status_trigger
BEFORE UPDATE OF status ON loans
FOR EACH ROW
DECLARE
    v_old_status loans.status%TYPE;
    v_new_status loans.status%TYPE;
BEGIN
    v_old_status := :OLD.status;
    v_new_status := :NEW.status;
    
    -- 상태 변경 로직
    IF v_old_status = 'NORMAL' AND v_new_status = 'OVERDUE' THEN
        -- 연체 처리
        loan_package.update_loan_status(:NEW.loan_id, 'OVERDUE');
    ELSIF v_old_status = 'RESERVED' AND v_new_status = 'NORMAL' THEN
        -- 예약에서 대출로 변경
        loan_package.update_loan_status(:NEW.loan_id, 'NORMAL');
    END IF;
END;
/ 