-- 사용자 정의 예외 선언
CREATE OR REPLACE PACKAGE loan_exceptions AS
    max_extends_exceeded EXCEPTION;
    overdue_loan_exception EXCEPTION;
    already_reserved_exception EXCEPTION;
    PRAGMA EXCEPTION_INIT(max_extends_exceeded, -20001);
    PRAGMA EXCEPTION_INIT(overdue_loan_exception, -20002);
    PRAGMA EXCEPTION_INIT(already_reserved_exception, -20003);
END loan_exceptions;
/

-- 대출 관련 패키지
CREATE OR REPLACE PACKAGE loan_package AS
    -- 사용자의 대출 목록 조회
    FUNCTION GET_USER_LOANS(p_user_id IN NUMBER) RETURN SYS_REFCURSOR;
    
    -- 특정 대출 정보 조회
    FUNCTION GET_LOAN_BY_ID(p_loan_id IN NUMBER) RETURN SYS_REFCURSOR;
    
    -- 대출 정보 저장 (신규/수정)
    FUNCTION SAVE_LOAN(
        p_loan_id IN NUMBER,
        p_user_id IN NUMBER,
        p_book_id IN NUMBER,
        p_borrow_date IN DATE,
        p_return_date IN DATE,
        p_extend_number IN NUMBER
    ) RETURN NUMBER;
    
    -- 대출 정보 삭제
    PROCEDURE DELETE_LOAN(p_loan_id IN NUMBER);
    
    -- 대출 연장 가능 여부 확인
    FUNCTION CAN_EXTEND_LOAN(p_loan_id IN NUMBER) RETURN BOOLEAN;
END loan_package;
/

CREATE OR REPLACE PACKAGE BODY loan_package AS
    -- 사용자의 대출 목록 조회
    FUNCTION GET_USER_LOANS(p_user_id IN NUMBER) RETURN SYS_REFCURSOR IS
        v_result SYS_REFCURSOR;
    BEGIN
        OPEN v_result FOR
            SELECT l.loan_id, l.user_id, l.book_id, l.borrow_date, l.return_date,
                   l.extend_number, l.created_at, l.updated_at,
                   b.title as book_title, b.author, b.publisher,
                   lib.name as library_name
            FROM loans l
            JOIN books b ON l.book_id = b.book_id
            JOIN libraries lib ON b.library_id = lib.library_id
            WHERE l.user_id = p_user_id
            ORDER BY l.borrow_date DESC;
        
        RETURN v_result;
    END GET_USER_LOANS;
    
    -- 특정 대출 정보 조회
    FUNCTION GET_LOAN_BY_ID(p_loan_id IN NUMBER) RETURN SYS_REFCURSOR IS
        v_result SYS_REFCURSOR;
    BEGIN
        OPEN v_result FOR
            SELECT l.loan_id, l.user_id, l.book_id, l.borrow_date, l.return_date,
                   l.extend_number, l.created_at, l.updated_at,
                   b.title as book_title, b.author, b.publisher,
                   lib.name as library_name
            FROM loans l
            JOIN books b ON l.book_id = b.book_id
            JOIN libraries lib ON b.library_id = lib.library_id
            WHERE l.loan_id = p_loan_id;
        
        RETURN v_result;
    END GET_LOAN_BY_ID;
    
    -- 대출 연장 가능 여부 확인
    FUNCTION CAN_EXTEND_LOAN(p_loan_id IN NUMBER) RETURN BOOLEAN IS
        v_extend_number NUMBER;
        v_return_date DATE;
    BEGIN
        SELECT extend_number, return_date
        INTO v_extend_number, v_return_date
        FROM loans
        WHERE loan_id = p_loan_id;
        
        -- 연장 횟수 체크
        IF v_extend_number >= 2 THEN
            RAISE loan_exceptions.max_extends_exceeded;
        END IF;
        
        -- 연체 여부 체크
        IF v_return_date < SYSDATE THEN
            RAISE loan_exceptions.overdue_loan_exception;
        END IF;
        
        RETURN TRUE;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RETURN FALSE;
    END CAN_EXTEND_LOAN;
    
    -- 대출 정보 저장 (신규/수정)
    FUNCTION SAVE_LOAN(
        p_loan_id IN NUMBER,
        p_user_id IN NUMBER,
        p_book_id IN NUMBER,
        p_borrow_date IN DATE,
        p_return_date IN DATE,
        p_extend_number IN NUMBER
    ) RETURN NUMBER IS
        v_loan_id NUMBER;
        v_current_time TIMESTAMP := SYSTIMESTAMP;
    BEGIN
        -- 연장인 경우 유효성 검사
        IF p_loan_id IS NOT NULL THEN
            IF NOT CAN_EXTEND_LOAN(p_loan_id) THEN
                RETURN NULL;
            END IF;
        END IF;
        
        -- 예약 중복 체크
        FOR r IN (
            SELECT 1
            FROM loans
            WHERE book_id = p_book_id
            AND return_date > SYSDATE
        ) LOOP
            RAISE loan_exceptions.already_reserved_exception;
        END LOOP;
        
        -- 신규/수정 처리
        IF p_loan_id IS NULL THEN
            -- 신규 대출
            INSERT INTO loans (
                user_id, book_id, borrow_date, return_date,
                extend_number, created_at, updated_at
            ) VALUES (
                p_user_id, p_book_id, p_borrow_date, p_return_date,
                p_extend_number, v_current_time, v_current_time
            ) RETURNING loan_id INTO v_loan_id;
        ELSE
            -- 대출 수정
            UPDATE loans
            SET return_date = p_return_date,
                extend_number = p_extend_number,
                updated_at = v_current_time
            WHERE loan_id = p_loan_id
            RETURNING loan_id INTO v_loan_id;
        END IF;
        
        RETURN v_loan_id;
    EXCEPTION
        WHEN loan_exceptions.max_extends_exceeded THEN
            RAISE_APPLICATION_ERROR(-20001, '대출 연장은 최대 2회까지만 가능합니다.');
        WHEN loan_exceptions.overdue_loan_exception THEN
            RAISE_APPLICATION_ERROR(-20002, '연체된 대출은 연장할 수 없습니다.');
        WHEN loan_exceptions.already_reserved_exception THEN
            RAISE_APPLICATION_ERROR(-20003, '이미 예약된 도서입니다.');
    END SAVE_LOAN;
    
    -- 대출 정보 삭제
    PROCEDURE DELETE_LOAN(p_loan_id IN NUMBER) IS
    BEGIN
        DELETE FROM loans WHERE loan_id = p_loan_id;
        
        IF SQL%ROWCOUNT = 0 THEN
            RAISE NO_DATA_FOUND;
        END IF;
    END DELETE_LOAN;
END loan_package;
/

-- 대출 연장 트리거
CREATE OR REPLACE TRIGGER loan_extension_trigger
BEFORE UPDATE OF return_date ON loans
FOR EACH ROW
WHEN (NEW.return_date > OLD.return_date)
DECLARE
    v_extend_number NUMBER;
BEGIN
    -- 연장 횟수 증가
    :NEW.extend_number := :OLD.extend_number + 1;
    
    -- 연장 횟수 체크
    IF :NEW.extend_number > 2 THEN
        RAISE loan_exceptions.max_extends_exceeded;
    END IF;
    
    -- 연체 여부 체크
    IF :OLD.return_date < SYSDATE THEN
        RAISE loan_exceptions.overdue_loan_exception;
    END IF;
EXCEPTION
    WHEN loan_exceptions.max_extends_exceeded THEN
        RAISE_APPLICATION_ERROR(-20001, '대출 연장은 최대 2회까지만 가능합니다.');
    WHEN loan_exceptions.overdue_loan_exception THEN
        RAISE_APPLICATION_ERROR(-20002, '연체된 대출은 연장할 수 없습니다.');
END;
/ 