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

    -- 대출
    PROCEDURE BORROW_BOOK (
        p_user_id IN NUMBER,
        p_book_id IN NUMBER,
        p_result OUT NUMBER,
        p_message OUT VARCHAR2,
        p_loan_id OUT NUMBER
    );

    -- 도서 반납
    PROCEDURE RETURN_BOOK (
        p_loan_id IN loans.loan_id%TYPE,
        p_result OUT NUMBER,
        p_message OUT VARCHAR2
    );

    FUNCTION CHECK_USER_LOAN_IS_AVAILABLE(p_user_id NUMBER)
    RETURN VARCHAR2;

    PROCEDURE RESERVE_BOOK (
        p_user_id IN NUMBER,
        p_book_id IN NUMBER,
        p_result OUT NUMBER,
        p_message OUT VARCHAR2
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
        p_resv_id IN reservations.resv_id%TYPE,
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

    -- 자동 반납 처리
    PROCEDURE AUTO_RETURN_OVERDUE_BOOKS;
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
                    WHEN l.status = 'BORROWED' THEN '대출중'
                    WHEN l.status = 'OVERDUE' THEN '연체'
                    WHEN l.status = 'RESERVED' THEN '예약'
                    ELSE l.status
                END as status,
                CASE
                    WHEN l.status = 'BORROWED' AND l.extend_number < 2 AND l.return_date > SYSTIMESTAMP THEN 1
                    ELSE 0
                END as is_extendable,
                CASE
                    WHEN l.status = 'BORROWED' AND l.extend_number < 2 AND l.return_date > SYSTIMESTAMP THEN '연장'
                    WHEN l.status = 'OVERDUE' THEN null
                    WHEN l.status = 'RESERVED' THEN '취소'
                    ELSE NULL
                END as action
            FROM loans l
            JOIN books b ON l.book_id = b.book_id
            JOIN libraries lib ON b.library_id = lib.library_id
            WHERE l.user_id = p_user_id
            AND l.status IN ('BORROWED', 'OVERDUE', 'RESERVED');
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
        IF v_status != 'BORROWED' THEN
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

    -- 대출 가능 사용자인지 체크
    FUNCTION CHECK_USER_LOAN_IS_AVAILABLE(p_user_id NUMBER)
    RETURN VARCHAR2
    IS
        -- %TYPE 사용
        v_user_id USERS.USER_ID%TYPE := p_user_id;
        v_current_loans NUMBER := 0;
        v_user_status USERS.IS_ACTIVE%TYPE;
        v_user_mileage USERS.MILEAGE%TYPE;

        -- %ROWTYPE 사용
        user_rec USERS%ROWTYPE;

        -- 커서 선언
        CURSOR c_user_loans IS
            SELECT LOAN_ID, BORROW_DATE, STATUS, BOOK_ID
            FROM LOANS
            WHERE USER_ID = v_user_id AND STATUS = 'BORROWED';

        loan_rec c_user_loans%ROWTYPE;

    BEGIN
        -- 사용자 정보 조회
        SELECT * INTO user_rec FROM USERS WHERE USER_ID = v_user_id;

        -- 사용자 활성 상태 체크
        IF user_rec.IS_ACTIVE = 'N' THEN
            RETURN '비활성 사용자입니다.';
        END IF;

        -- 현재 대출 건수 체크
        OPEN c_user_loans;
        LOOP
            FETCH c_user_loans INTO loan_rec;
            EXIT WHEN c_user_loans%NOTFOUND;
            v_current_loans := v_current_loans + 1;
        END LOOP;
        CLOSE c_user_loans;

        -- 조건문 사용
        IF v_current_loans >= 5 THEN -- E-BOOK은 5권까지 대출 가능
            RETURN '대출 한도 초과 (최대 5권)';
        ELSE
            RETURN 'OK';
        END IF;

    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RETURN '존재하지 않는 사용자입니다.';
        WHEN OTHERS THEN
            RETURN '오류 발생: ' || SQLERRM;
    END CHECK_USER_LOAN_IS_AVAILABLE;

    -- 도서 대출 (중복 대출 체크 추가)
    PROCEDURE BORROW_BOOK (
        p_user_id   IN  NUMBER,
        p_book_id   IN  NUMBER,
        p_result    OUT NUMBER,
        p_message   OUT VARCHAR2,
        p_loan_id   OUT NUMBER
    )
    IS
        -- 변수 선언
        v_user_id USERS.USER_ID%TYPE := p_user_id;
        v_book_id BOOKS.BOOK_ID%TYPE := p_book_id;
        v_loan_id LOANS.LOAN_ID%TYPE;
        v_amount  BOOKS.AVAILABLE_AMOUNT%TYPE;
        v_return_date TIMESTAMP;
        v_duplicate_count NUMBER := 0;  -- 중복 대출 체크용

        -- 레코드 타입
        book_rec BOOKS%ROWTYPE;
        user_rec USERS%ROWTYPE;

        -- 함수 결과 메시지
        v_loan_available_msg VARCHAR2(100);

        -- 예외 정의
        BOOK_NOT_AVAILABLE EXCEPTION;
        USER_NOT_ELIGIBLE   EXCEPTION;
        DUPLICATE_LOAN     EXCEPTION;  -- 중복 대출 예외 추가

    BEGIN
        -- NULL 체크
        IF p_user_id IS NULL OR p_book_id IS NULL THEN
            p_result := 0;
            p_message := '사용자 ID와 도서 ID는 필수입니다.';
            p_loan_id := NULL;
            RETURN;
        END IF;

        -- ★★★ 중복 대출 체크 추가 ★★★
        SELECT COUNT(*) INTO v_duplicate_count
        FROM LOANS
        WHERE USER_ID = v_user_id
          AND BOOK_ID = v_book_id
          AND STATUS IN ('BORROWED', 'OVERDUE');  -- 대출중이거나 연체인 경우

        IF v_duplicate_count > 0 THEN
            RAISE DUPLICATE_LOAN;
        END IF;

        -- 사용자 대출 가능 여부 확인 함수 호출
        v_loan_available_msg := CHECK_USER_LOAN_IS_AVAILABLE(v_user_id);

        IF v_loan_available_msg != 'OK' THEN
            RAISE USER_NOT_ELIGIBLE;
        END IF;

        -- 도서 정보 조회
        BEGIN
            SELECT * INTO book_rec
            FROM BOOKS
            WHERE BOOK_ID = v_book_id
            FOR UPDATE;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                p_result := 0;
                p_message := '존재하지 않는 도서입니다.';
                p_loan_id := NULL;
                RETURN;
        END;

        -- 도서 수량 확인
        IF book_rec.AVAILABLE_AMOUNT <= 0 THEN
            RAISE BOOK_NOT_AVAILABLE;
        END IF;

        -- 반납일 계산
        v_return_date := SYSTIMESTAMP + INTERVAL '14' DAY;

        -- INSERT + RETURNING ID
        INSERT INTO LOANS (USER_ID, BOOK_ID, BORROW_DATE, RETURN_DATE, STATUS)
        VALUES (v_user_id, v_book_id, SYSTIMESTAMP, v_return_date, 'BORROWED')
        RETURNING LOAN_ID INTO v_loan_id;

        -- 수량 감소
        UPDATE BOOKS
        SET AVAILABLE_AMOUNT = AVAILABLE_AMOUNT - 1,
            UPDATED_AT = SYSTIMESTAMP
        WHERE BOOK_ID = v_book_id;

        COMMIT;

        -- OUT 파라미터 설정
        p_result := 1;
        p_message := '대출이 완료되었습니다.';
        p_loan_id := v_loan_id;

    EXCEPTION
        WHEN DUPLICATE_LOAN THEN
            ROLLBACK;
            p_result := 0;
            p_message := '이미 대출 중인 도서입니다.';
            p_loan_id := NULL;
        WHEN BOOK_NOT_AVAILABLE THEN
            ROLLBACK;
            p_result := 0;
            p_message := '대출 불가능한 도서입니다.';
            p_loan_id := NULL;
        WHEN USER_NOT_ELIGIBLE THEN
            ROLLBACK;
            p_result := 0;
            p_message := v_loan_available_msg;
            p_loan_id := NULL;
        WHEN OTHERS THEN
            ROLLBACK;
            p_result := 0;
            p_message := '대출 중 오류 발생: ' || SQLERRM;
            p_loan_id := NULL;
    END BORROW_BOOK;

    -- 도서 예약
    PROCEDURE RESERVE_BOOK (
        p_user_id IN NUMBER,
        p_book_id IN NUMBER,
        p_result OUT NUMBER,
        p_message OUT VARCHAR2
    )
    IS
        v_amount BOOKS.AVAILABLE_AMOUNT%TYPE;
    BEGIN
        SELECT AVAILABLE_AMOUNT INTO v_amount FROM BOOKS WHERE BOOK_ID = p_book_id;

        IF v_amount > 0 THEN
            p_result := 0;
            p_message := '도서가 대출 가능 상태입니다. 예약할 수 없습니다.';
            RETURN;
        END IF;

        -- 트리거가 ID 자동 생성
        INSERT INTO RESERVATIONS (USER_ID, BOOK_ID, STATUS, RESV_DATE)
        VALUES (p_user_id, p_book_id, 'ACTIVE', SYSTIMESTAMP);

        COMMIT;

        p_result := 1;
        p_message := '예약이 완료되었습니다.';

    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            p_result := 0;
            p_message := '도서 정보가 존재하지 않습니다.';
        WHEN OTHERS THEN
            ROLLBACK;
            p_result := 0;
            p_message := '예약 중 오류 발생: ' || SQLERRM;
    END RESERVE_BOOK;

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
        SET return_date = return_date + INTERVAL '7' DAY,
            extend_number = extend_number + 1,
            updated_at = SYSTIMESTAMP
        WHERE loan_id = p_loan_id;

        COMMIT;

        -- 연장 후 결과 반환
        OPEN p_result FOR
            SELECT
                l.loan_id AS id,
                l.user_id,
                l.book_id,
                l.borrow_date,
                l.return_date,
                CASE
                    WHEN l.return_date < SYSTIMESTAMP THEN 'OVERDUE'
                    ELSE 'BORROWED'
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

    PROCEDURE cancel_reservation(
    p_resv_id IN reservations.resv_id%TYPE,
    p_result OUT loan_cursor
) IS
    v_reservation reservations%ROWTYPE;
    v_count NUMBER := 0;
BEGIN
    -- 디버깅: 실제 전달된 파라미터 확인
    DBMS_OUTPUT.PUT_LINE('=== 예약 취소 시작 ===');
    DBMS_OUTPUT.PUT_LINE('전달받은 resv_id: ' || p_resv_id);

    -- 먼저 해당 ID가 존재하는지 확인
    SELECT COUNT(*) INTO v_count
    FROM reservations
    WHERE resv_id = p_resv_id;

    DBMS_OUTPUT.PUT_LINE('찾은 예약 개수: ' || v_count);

    IF v_count = 0 THEN
        DBMS_OUTPUT.PUT_LINE('예약을 찾을 수 없음!');
        RAISE_APPLICATION_ERROR(-20001, '예약 ID ' || p_resv_id || '를 찾을 수 없습니다.');
    END IF;

    -- 예약 정보 조회
    SELECT * INTO v_reservation
    FROM reservations
    WHERE resv_id = p_resv_id;

    DBMS_OUTPUT.PUT_LINE('예약 정보 조회 성공 - 상태: ' || v_reservation.status);

    -- 결과 반환
    OPEN p_result FOR
        SELECT
            v_reservation.resv_id AS "ID",      -- 대문자로 수정
            v_reservation.user_id AS "USER_ID",
            v_reservation.book_id AS "BOOK_ID",
            v_reservation.resv_date AS "RESV_DATE",
            'CANCELLED' AS "STATUS"
        FROM dual;

    -- 예약 취소 처리
    DELETE FROM reservations
    WHERE resv_id = p_resv_id;

    DBMS_OUTPUT.PUT_LINE('예약 삭제 완료');
    COMMIT;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        DBMS_OUTPUT.PUT_LINE('NO_DATA_FOUND 예외 발생!');
        RAISE_APPLICATION_ERROR(-20004, '예약 ID ' || p_resv_id || '를 찾을 수 없습니다.');
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('기타 예외: ' || SQLERRM);
        ROLLBACK;
        RAISE;
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

    -- 도서 반납
    PROCEDURE RETURN_BOOK (
        p_loan_id IN loans.loan_id%TYPE,
        p_result OUT NUMBER,
        p_message OUT VARCHAR2
    ) IS
        v_loan loans%ROWTYPE;
        v_mileage NUMBER := 3; 
        v_total_mileage NUMBER;
        v_log_id reading_log.log_id%TYPE;
    BEGIN
        -- 대출 정보 조회
        SELECT * INTO v_loan
        FROM loans
        WHERE loan_id = p_loan_id;

        IF v_loan.status = 'RETURNED' THEN
            p_result := 0;
            p_message := '이미 반납된 도서입니다.';
            RETURN;
        END IF;
        
        -- 사용자의 총 마일리지 조회
        SELECT mileage INTO v_total_mileage
        FROM users
        WHERE user_id = v_loan.user_id;

        -- reading_log에 저장
        v_log_id := reading_log_package.save_reading_log(
            p_user_id => v_loan.user_id,
            p_book_id => v_loan.book_id,
            p_borrow_date => v_loan.borrow_date,
            p_return_date => SYSTIMESTAMP,
            p_mileage => v_mileage,
            p_total_mileage => v_total_mileage + v_mileage
        );

        -- 사용자 마일리지 업데이트
        UPDATE users
        SET mileage = mileage + v_mileage,
            updated_at = SYSTIMESTAMP
        WHERE user_id = v_loan.user_id;

        -- 대출 상태 업데이트
        UPDATE loans
        SET status = 'RETURNED',
            updated_at = SYSTIMESTAMP
        WHERE loan_id = p_loan_id;

        -- 도서 수량 증가
        UPDATE books
        SET available_amount = available_amount + 1,
            updated_at = SYSTIMESTAMP
        WHERE book_id = v_loan.book_id;

        COMMIT;

        p_result := 1;
        p_message := '반납이 완료되었습니다. 마일리지 3점이 적립되었습니다.';

    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            ROLLBACK;
            p_result := 0;
            p_message := '대출 정보를 찾을 수 없습니다.';
        WHEN OTHERS THEN
            ROLLBACK;
            p_result := 0;
            p_message := '반납 중 오류 발생: ' || SQLERRM;
    END RETURN_BOOK;

    -- 자동 반납 처리
    PROCEDURE AUTO_RETURN_OVERDUE_BOOKS IS
        CURSOR c_overdue_loans IS
            SELECT l.*
            FROM loans l
            WHERE l.status = 'BORROWED'
            AND l.return_date < SYSTIMESTAMP;
            
        v_result NUMBER;
        v_message VARCHAR2(200);
    BEGIN
        FOR loan_rec IN c_overdue_loans LOOP
            -- 각 연체 도서에 대해 반납 처리
            RETURN_BOOK(
                p_loan_id => loan_rec.loan_id,
                p_result => v_result,
                p_message => v_message
            );
            
            -- 로그 기록
            INSERT INTO system_logs (
                log_type,
                message,
                created_at
            ) VALUES (
                'AUTO_RETURN',
                '도서 ID: ' || loan_rec.book_id || ' 자동 반납 처리 - ' || v_message,
                SYSTIMESTAMP
            );
        END LOOP;
        
        COMMIT;
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            -- 오류 로그 기록
            INSERT INTO system_logs (
                log_type,
                message,
                created_at
            ) VALUES (
                'AUTO_RETURN_ERROR',
                '자동 반납 처리 중 오류 발생: ' || SQLERRM,
                SYSTIMESTAMP
            );
            COMMIT;
    END AUTO_RETURN_OVERDUE_BOOKS;

END loan_package;  -- 여기서 패키지 바디 전체가 끝남!
/

-- loans 테이블 INSERT 트리거
CREATE OR REPLACE TRIGGER loans_insert_trigger
AFTER INSERT ON loans
FOR EACH ROW
DECLARE
    v_total_mileage NUMBER;
    v_log_id reading_log.log_id%TYPE;
BEGIN
    -- 사용자의 총 마일리지 조회
    SELECT mileage INTO v_total_mileage
    FROM users
    WHERE user_id = :NEW.user_id;

    -- reading_log에 저장
    v_log_id := reading_log_package.save_reading_log(
        p_user_id => :NEW.user_id,
        p_book_id => :NEW.book_id,
        p_borrow_date => :NEW.borrow_date,
        p_return_date => :NEW.return_date,
        p_mileage => CASE 
            WHEN :NEW.status = 'RETURNED' THEN 3  -- 반납된 경우 3점
            ELSE 0  -- 대출 중인 경우 0점
        END,
        p_total_mileage => CASE 
            WHEN :NEW.status = 'RETURNED' THEN v_total_mileage + 3  -- 반납된 경우 마일리지 추가
            ELSE v_total_mileage  -- 대출 중인 경우 현재 마일리지
        END
    );

    -- 반납된 경우 사용자 마일리지 업데이트
    IF :NEW.status = 'RETURNED' THEN
        UPDATE users
        SET mileage = mileage + 3,
            updated_at = SYSTIMESTAMP
        WHERE user_id = :NEW.user_id;
    END IF;
END;
/