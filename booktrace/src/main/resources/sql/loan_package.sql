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
        borrow_date TIMESTAMP,
        return_date TIMESTAMP,
        status VARCHAR2(20),
        extend_number NUMBER,
        created_at TIMESTAMP,
        updated_at TIMESTAMP,
        book_title books.title%TYPE,
        library_name libraries.name%TYPE
    );
    
    -- 사용자의 대출 목록 조회
    PROCEDURE get_user_loans(
        p_user_id IN loans.user_id%TYPE,
        p_result OUT loan_cursor
    );
    
    -- 특정 대출 정보 조회
    PROCEDURE get_loan_by_id(
        p_loan_id IN loans.loan_id%TYPE,
        p_result OUT loan_cursor
    );
    
    -- 대출 연장
    PROCEDURE extend_loan(
        p_loan_id IN loans.loan_id%TYPE,
        p_result OUT loan_cursor
    );
    
    -- 예약 취소
    PROCEDURE cancel_reservation(
        p_reservation_id IN reservations.reservation_id%TYPE,
        p_result OUT loan_cursor
    );
    
    -- 도서 예약 여부 확인
    FUNCTION has_reservation(
        p_book_id IN books.book_id%TYPE
    ) RETURN BOOLEAN;
    
    -- 대출 상태 업데이트
    PROCEDURE update_loan_status(
        p_loan_id IN loans.loan_id%TYPE,
        p_status IN VARCHAR2
    );
    
    -- 대출 연장 가능 여부 확인
    FUNCTION can_extend_loan(
        p_loan_id IN loans.loan_id%TYPE
    ) RETURN NUMBER;
    
    -- 대출 정보 저장 (신규/수정)
    FUNCTION save_loan(
        p_loan_id IN loans.loan_id%TYPE,
        p_user_id IN loans.user_id%TYPE,
        p_book_id IN books.book_id%TYPE,
        p_borrow_date IN TIMESTAMP,
        p_return_date IN TIMESTAMP,
        p_extend_number IN NUMBER,
        p_status IN VARCHAR2
    ) RETURN loans.loan_id%TYPE;
    
    -- 대출 정보 삭제
    PROCEDURE delete_loan(
        p_loan_id IN loans.loan_id%TYPE
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
    BEGIN
        OPEN p_result FOR
            SELECT 
                ROW_NUMBER() OVER (ORDER BY l.borrow_date DESC) as no,
                l.loan_id,
                l.borrow_date as loan_date,
                l.return_date as due_date,
                b.title as book_title,
                lib.name as library_location,
                CASE 
                    WHEN l.status = 'NORMAL' THEN '정상'
                    WHEN l.status = 'OVERDUE' THEN '연체'
                    WHEN l.status = 'RESERVED' THEN '예약'
                    ELSE l.status
                END as status,
                CASE 
                    WHEN l.status = 'NORMAL' AND l.extend_number < 2 AND l.return_date > SYSTIMESTAMP THEN TRUE
                    ELSE FALSE
                END as is_extendable,
                CASE 
                    WHEN l.status = 'NORMAL' AND l.extend_number < 2 AND l.return_date > SYSTIMESTAMP THEN '연장'
                    WHEN l.status = 'OVERDUE' THEN null
                    WHEN l.status = 'RESERVED' THEN '취소'
                    ELSE NULL
                END as action
            FROM loans l
            JOIN books b ON l.book_id = b.book_id
            JOIN libraries lib ON b.library_id = lib.library_id
            WHERE l.user_id = p_user_id;
    END get_user_loans;

    -- 대출 연장 가능 여부 확인
    FUNCTION can_extend_loan(
        p_loan_id IN loans.loan_id%TYPE
    ) RETURN NUMBER IS
        v_extend_number NUMBER;
        v_return_date TIMESTAMP;
        v_status VARCHAR2(20);
    BEGIN
        -- 대출 정보 조회
        SELECT extend_number, return_date, status
        INTO v_extend_number, v_return_date, v_status
        FROM loans
        WHERE loan_id = p_loan_id;

        -- 연장 횟수 체크
        IF v_extend_number >= 2 THEN
            RAISE loan_exceptions.max_extends_exceeded;
        END IF;

        -- 연체 여부 체크
        IF v_return_date < SYSTIMESTAMP THEN
            RAISE loan_exceptions.overdue_loan_exception;
        END IF;

        -- 상태 체크
        IF v_status != 'NORMAL' THEN
            RETURN 0;
        END IF;

        RETURN 1;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE loan_exceptions.not_found_exception;
        WHEN loan_exceptions.max_extends_exceeded THEN
            RAISE_APPLICATION_ERROR(-20001, '대출 연장은 최대 2회까지만 가능합니다.');
        WHEN loan_exceptions.overdue_loan_exception THEN
            RAISE_APPLICATION_ERROR(-20002, '연체된 대출은 연장할 수 없습니다.');
    END can_extend_loan;

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
    BEGIN
        -- 연장 가능 여부 확인
        IF can_extend_loan(p_loan_id) = 0 THEN
            RETURN;
        END IF;

        -- 대출 정보 조회
        SELECT * INTO v_loan
        FROM loans
        WHERE loan_id = p_loan_id;

        -- 예약 여부 확인
        IF has_reservation(v_loan.book_id) THEN
            RAISE loan_exceptions.already_reserved_exception;
        END IF;

        -- 연장 처리
        UPDATE loans
        SET return_date = return_date + 7,  -- INTERVAL 대신 + 7 사용
            extend_number = extend_number + 1,
            updated_at = SYSTIMESTAMP
        WHERE loan_id = p_loan_id;

        COMMIT;

        -- 연장 후 결과 반환 (명시적 컬럼 나열)
        OPEN p_result FOR
            SELECT
                l.loan_id AS id,
                l.user_id,
                l.book_id,
                l.borrow_date,
                l.return_date,
                CASE
                    WHEN l.return_date < SYSDATE THEN 'OVERDUE'
                    ELSE 'NORMAL'
                END AS status,
                l.extend_number AS extensionCount,
                b.title AS book_title,
                lib.name AS library_name
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
        p_reservation_id IN reservations.reservation_id%TYPE,
        p_result OUT loan_cursor
    ) IS
        v_reservation reservations%ROWTYPE;
    BEGIN
        -- 예약 정보 조회
        SELECT * INTO v_reservation
        FROM reservations
        WHERE reservation_id = p_reservation_id;

        -- 삭제 전에 필요한 정보만 따로 보관
        OPEN p_result FOR
            SELECT
                v_reservation.reservation_id AS id,
                v_reservation.user_id,
                v_reservation.book_id,
                v_reservation.reservation_date AS resv_date,
                'CANCELLED' AS status
            FROM dual;

        -- 예약 취소 처리
        DELETE FROM reservations
        WHERE reservation_id = p_reservation_id;
        COMMIT;
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
        FROM reservations
        WHERE book_id = p_book_id;
        
        RETURN v_count > 0;
    END has_reservation;
    
    -- 대출 상태 업데이트
    PROCEDURE update_loan_status(
        p_loan_id IN loans.loan_id%TYPE,
        p_status IN VARCHAR2
    ) IS
    BEGIN
        UPDATE loans
        SET status = p_status,
            updated_at = SYSTIMESTAMP
        WHERE loan_id = p_loan_id;
        
        IF SQL%ROWCOUNT = 0 THEN
            RAISE loan_exceptions.not_found_exception;
        END IF;
    END update_loan_status;
    
    -- 대출 정보 저장 (신규/수정)
    FUNCTION save_loan(
        p_loan_id IN loans.loan_id%TYPE,
        p_user_id IN loans.user_id%TYPE,
        p_book_id IN books.book_id%TYPE,
        p_borrow_date IN TIMESTAMP,
        p_return_date IN TIMESTAMP,
        p_extend_number IN NUMBER,
        p_status IN VARCHAR2
    ) RETURN loans.loan_id%TYPE IS
        v_loan_id loans.loan_id%TYPE;
        v_current_time TIMESTAMP := SYSTIMESTAMP;
    BEGIN
        -- 연장인 경우 유효성 검사
        IF p_loan_id IS NOT NULL THEN
            IF can_extend_loan(p_loan_id) = 0 THEN
                RETURN NULL;
            END IF;
        END IF;
        
        -- 예약 중복 체크
        IF p_status = 'RESERVED' AND has_reservation(p_book_id) THEN
            RAISE loan_exceptions.already_reserved_exception;
        END IF;
        
        -- 신규/수정 처리
        IF p_loan_id IS NULL THEN
            -- 신규 대출
            INSERT INTO loans (
                user_id, book_id, borrow_date, return_date,
                extend_number, status, created_at, updated_at
            ) VALUES (
                p_user_id, p_book_id, p_borrow_date, p_return_date,
                p_extend_number, p_status, v_current_time, v_current_time
            ) RETURNING loan_id INTO v_loan_id;
        ELSE
            -- 대출 수정
            UPDATE loans
            SET return_date = p_return_date,
                extend_number = p_extend_number,
                status = p_status,
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
    END save_loan;
    
    -- 대출 정보 삭제
    PROCEDURE delete_loan(
        p_loan_id IN loans.loan_id%TYPE
    ) IS
    BEGIN
        DELETE FROM loans WHERE loan_id = p_loan_id;
        
        IF SQL%ROWCOUNT = 0 THEN
            RAISE loan_exceptions.not_found_exception;
        END IF;
    END delete_loan;
END loan_package;
/

-- 대출 상태 변경 트리거
CREATE OR REPLACE TRIGGER loan_status_trigger
BEFORE UPDATE OF status ON loans
FOR EACH ROW
DECLARE
    v_old_status VARCHAR2(20);
    v_new_status VARCHAR2(20);
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
    IF :OLD.return_date < SYSTIMESTAMP THEN
        RAISE loan_exceptions.overdue_loan_exception;
    END IF;
EXCEPTION
    WHEN loan_exceptions.max_extends_exceeded THEN
        RAISE_APPLICATION_ERROR(-20001, '대출 연장은 최대 2회까지만 가능합니다.');
    WHEN loan_exceptions.overdue_loan_exception THEN
        RAISE_APPLICATION_ERROR(-20002, '연체된 대출은 연장할 수 없습니다.');
END;
/ 