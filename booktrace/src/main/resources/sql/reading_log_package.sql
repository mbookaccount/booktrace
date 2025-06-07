-- 예외 정의
CREATE OR REPLACE PACKAGE reading_log_exceptions AS
    not_found_exception EXCEPTION;
    invalid_date_exception EXCEPTION;
    
    PRAGMA EXCEPTION_INIT(not_found_exception, -30001);
    PRAGMA EXCEPTION_INIT(invalid_date_exception, -30002);
END reading_log_exceptions;
/

-- 독서 로그 관련 패키지
CREATE OR REPLACE PACKAGE reading_log_package AS
    -- 커서 타입 정의
    TYPE reading_log_cursor IS REF CURSOR;
    
    -- 독서 로그 정보를 담을 레코드 타입
    TYPE reading_log_record IS RECORD (
        log_id reading_log.log_id%TYPE,
        user_id reading_log.user_id%TYPE,
        book_id reading_log.book_id%TYPE,
        book_title books.title%TYPE,
        borrow_date DATE,
        return_date DATE,
        mileage NUMBER,
        total_mileage NUMBER,
        created_at DATE,
        updated_at DATE
    );
    
    -- 사용자의 독서 로그 목록 조회
    PROCEDURE get_user_reading_logs(
        p_user_id IN reading_log.user_id%TYPE,
        p_result OUT reading_log_cursor
    );
    
    -- 독서 로그 저장
    FUNCTION save_reading_log(
        p_user_id IN reading_log.user_id%TYPE,
        p_book_id IN reading_log.book_id%TYPE,
        p_borrow_date IN DATE,
        p_return_date IN DATE,
        p_mileage IN NUMBER,
        p_total_mileage IN NUMBER
    ) RETURN reading_log.log_id%TYPE;
    
    -- 독서 로그 삭제
    PROCEDURE delete_reading_log(
        p_log_id IN reading_log.log_id%TYPE
    );
    
    -- 독서 로그 수정
    PROCEDURE update_reading_log(
        p_log_id IN reading_log.log_id%TYPE,
        p_return_date IN DATE,
        p_mileage IN NUMBER,
        p_total_mileage IN NUMBER
    );
END reading_log_package;
/

-- 패키지 본문
CREATE OR REPLACE PACKAGE BODY reading_log_package AS
    -- 사용자의 독서 로그 목록 조회
    PROCEDURE get_user_reading_logs(
        p_user_id IN reading_log.user_id%TYPE,
        p_result OUT reading_log_cursor
    ) IS
    BEGIN
        OPEN p_result FOR
            SELECT 
                ROW_NUMBER() OVER (ORDER BY rl.borrow_date DESC) as user_log_number,
                rl.log_id,
                rl.user_id,
                rl.book_id,
                b.title as book_title,
                rl.borrow_date,
                rl.return_date,
                rl.mileage,
                rl.total_mileage,
                rl.created_at,
                rl.updated_at
            FROM reading_log rl
            JOIN books b ON rl.book_id = b.book_id
            WHERE rl.user_id = p_user_id;
    END get_user_reading_logs;
    
    -- 독서 로그 저장
    FUNCTION save_reading_log(
        p_user_id IN reading_log.user_id%TYPE,
        p_book_id IN reading_log.book_id%TYPE,
        p_borrow_date IN DATE,
        p_return_date IN DATE,
        p_mileage IN NUMBER,
        p_total_mileage IN NUMBER
    ) RETURN reading_log.log_id%TYPE IS
        v_log_id reading_log.log_id%TYPE;
        v_current_time DATE := SYSDATE;
    BEGIN
        -- 날짜 유효성 검사
        IF p_borrow_date > p_return_date THEN
            RAISE reading_log_exceptions.invalid_date_exception;
        END IF;
        
        -- 독서 로그 저장
        INSERT INTO reading_log (
            user_id, book_id, borrow_date, return_date,
            mileage, total_mileage, created_at, updated_at
        ) VALUES (
            p_user_id, p_book_id, p_borrow_date, p_return_date,
            p_mileage, p_total_mileage, v_current_time, v_current_time
        ) RETURNING log_id INTO v_log_id;
        
        RETURN v_log_id;
    EXCEPTION
        WHEN reading_log_exceptions.invalid_date_exception THEN
            RAISE_APPLICATION_ERROR(-30002, '반납일은 대출일보다 이후여야 합니다.');
    END save_reading_log;
    
    -- 독서 로그 삭제
    PROCEDURE delete_reading_log(
        p_log_id IN reading_log.log_id%TYPE
    ) IS
    BEGIN
        DELETE FROM reading_log WHERE log_id = p_log_id;
        
        IF SQL%ROWCOUNT = 0 THEN
            RAISE reading_log_exceptions.not_found_exception;
        END IF;
    END delete_reading_log;
    
    -- 독서 로그 수정
    PROCEDURE update_reading_log(
        p_log_id IN reading_log.log_id%TYPE,
        p_return_date IN DATE,
        p_mileage IN NUMBER,
        p_total_mileage IN NUMBER
    ) IS
        v_borrow_date DATE;
    BEGIN
        -- 대출일 조회
        SELECT borrow_date INTO v_borrow_date
        FROM reading_log
        WHERE log_id = p_log_id;
        
        -- 날짜 유효성 검사
        IF v_borrow_date > p_return_date THEN
            RAISE reading_log_exceptions.invalid_date_exception;
        END IF;
        
        -- 독서 로그 수정
        UPDATE reading_log
        SET return_date = p_return_date,
            mileage = p_mileage,
            total_mileage = p_total_mileage,
            updated_at = SYSDATE
        WHERE log_id = p_log_id;
        
        IF SQL%ROWCOUNT = 0 THEN
            RAISE reading_log_exceptions.not_found_exception;
        END IF;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE reading_log_exceptions.not_found_exception;
        WHEN reading_log_exceptions.invalid_date_exception THEN
            RAISE_APPLICATION_ERROR(-30002, '반납일은 대출일보다 이후여야 합니다.');
    END update_reading_log;
END reading_log_package;
/ 