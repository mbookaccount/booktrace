CREATE OR REPLACE PACKAGE return_exceptions AS
    loan_not_found_exception EXCEPTION;
    already_returned_exception EXCEPTION;
    invalid_status_exception EXCEPTION;
    return_processing_exception EXCEPTION;

    PRAGMA EXCEPTION_INIT(loan_not_found_exception, -20101);
    PRAGMA EXCEPTION_INIT(already_returned_exception, -20102);
    PRAGMA EXCEPTION_INIT(invalid_status_exception, -20103);
    PRAGMA EXCEPTION_INIT(return_processing_exception, -20104);
END return_exceptions;
/

CREATE OR REPLACE PACKAGE return_package AS
    -- 커서 타입 정의
    TYPE return_cursor IS REF CURSOR;

    -- 반납 정보를 담을 레코드 타입
    TYPE return_record IS RECORD (
        loan_id loans.loan_id%TYPE,
        user_id loans.user_id%TYPE,
        book_id loans.book_id%TYPE,
        book_title books.title%TYPE,
        borrow_date TIMESTAMP,
        return_date TIMESTAMP,
        actual_return_date TIMESTAMP,
        days_overdue NUMBER,
        mileage_earned NUMBER,
        library_name libraries.name%TYPE
    );

    -- 도서 반납
    PROCEDURE return_book(
        p_loan_id IN loans.loan_id%TYPE,
        p_result OUT NUMBER,
        p_message OUT VARCHAR2
    );

    -- 반납 가능한 대출 목록 조회
    PROCEDURE get_returnable_loans(
        p_user_id IN loans.user_id%TYPE,
        p_result OUT return_cursor
    );

    -- 반납 이력 조회
    PROCEDURE get_return_history(
        p_user_id IN loans.user_id%TYPE,
        p_result OUT return_cursor
    );

    -- 반납 가능 여부 확인
    FUNCTION can_return_book(
        p_loan_id IN loans.loan_id%TYPE
    ) RETURN NUMBER;

END return_package;
/

CREATE OR REPLACE PACKAGE BODY return_package AS

    -- 도서 반납
    PROCEDURE return_book(
        p_loan_id IN loans.loan_id%TYPE,
        p_result OUT NUMBER,
        p_message OUT VARCHAR2
    ) IS
        v_count NUMBER := 0;
        v_status VARCHAR2(20);
        v_book_title books.title%TYPE;
    BEGIN
        -- 입력값 검증
        IF p_loan_id IS NULL THEN
            p_result := 0;
            p_message := '대출 ID는 필수입니다.';
            RETURN;
        END IF;

        -- 대출 정보 존재 여부 및 상태 확인
        SELECT COUNT(*), MAX(l.status), MAX(b.title)
        INTO v_count, v_status, v_book_title
        FROM loans l
        JOIN books b ON l.book_id = b.book_id
        WHERE l.loan_id = p_loan_id;

        -- 각 상황별 예외 발생
        IF v_count = 0 THEN
            RAISE return_exceptions.loan_not_found_exception;
        END IF;

        IF v_status = 'RETURNED' THEN
            RAISE return_exceptions.already_returned_exception;
        END IF;

        IF v_status NOT IN ('BORROWED') THEN
            RAISE return_exceptions.invalid_status_exception;
        END IF;

        -- 반납 처리
        UPDATE loans
        SET status = 'RETURNED',
            updated_at = SYSTIMESTAMP
        WHERE loan_id = p_loan_id;

        COMMIT;

        -- 성공 결과
        p_result := 1;
        p_message := '『' || v_book_title || '』 반납이 완료되었습니다.';

    EXCEPTION
        WHEN return_exceptions.loan_not_found_exception THEN
            ROLLBACK;
            p_result := 0;
            p_message := '존재하지 않는 대출입니다.';

        WHEN return_exceptions.already_returned_exception THEN
            ROLLBACK;
            p_result := 0;
            p_message := '『' || v_book_title || '』은(는) 이미 반납된 도서입니다.';

        WHEN return_exceptions.invalid_status_exception THEN
            ROLLBACK;
            p_result := 0;
            p_message := '반납할 수 없는 상태입니다. (현재 상태: ' || v_status || ')';

        WHEN OTHERS THEN
            ROLLBACK;
            p_result := 0;
            p_message := '반납 처리 중 오류 발생: ' || SQLERRM;
    END return_book;

    -- 반납 가능한 대출 목록 조회
    PROCEDURE get_returnable_loans(
        p_user_id IN loans.user_id%TYPE,
        p_result OUT return_cursor
    ) IS
    BEGIN
        OPEN p_result FOR
            SELECT
                ROW_NUMBER() OVER (ORDER BY l.borrow_date DESC) as no,
                l.loan_id,
                l.borrow_date,
                l.return_date as due_date,
                b.title as book_title,
                lib.name as library_location,
                CASE
                    WHEN l.status = 'BORROWED' AND l.return_date >= SYSTIMESTAMP THEN '대출중'
                    WHEN l.status = 'BORROWED' AND l.return_date < SYSTIMESTAMP THEN '연체'
                    WHEN l.status = 'OVERDUE' THEN '연체'
                    ELSE l.status
                END as status,
                CASE
                    WHEN l.return_date < SYSTIMESTAMP THEN
                        EXTRACT(DAY FROM (SYSTIMESTAMP - l.return_date))
                    ELSE 0
                END as days_overdue,
                '반납' as action
            FROM loans l
            JOIN books b ON l.book_id = b.book_id
            JOIN libraries lib ON b.library_id = lib.library_id
            WHERE l.user_id = p_user_id
              AND l.status IN ('BORROWED', 'OVERDUE')
            ORDER BY l.borrow_date DESC;
    END get_returnable_loans;

    -- 반납 이력 조회
    PROCEDURE get_return_history(
        p_user_id IN loans.user_id%TYPE,
        p_result OUT return_cursor
    ) IS
    BEGIN
        OPEN p_result FOR
            SELECT
                ROW_NUMBER() OVER (ORDER BY l.updated_at DESC) as no,
                l.loan_id,
                l.borrow_date,
                l.return_date as due_date,
                l.updated_at as actual_return_date,
                b.title as book_title,
                lib.name as library_location,
                CASE
                    WHEN l.updated_at > l.return_date THEN
                        EXTRACT(DAY FROM (l.updated_at - l.return_date))
                    ELSE 0
                END as days_overdue,
                CASE
                    WHEN l.updated_at <= l.return_date THEN 5
                    ELSE GREATEST(1, 5 - EXTRACT(DAY FROM (l.updated_at - l.return_date)))
                END as mileage_earned,
                '반납완료' as status
            FROM loans l
            JOIN books b ON l.book_id = b.book_id
            JOIN libraries lib ON b.library_id = lib.library_id
            WHERE l.user_id = p_user_id
              AND l.status = 'RETURNED'
            ORDER BY l.updated_at DESC;
    END get_return_history;

    -- 반납 가능 여부 확인
    FUNCTION can_return_book(
        p_loan_id IN loans.loan_id%TYPE
    ) RETURN NUMBER IS
        v_status VARCHAR2(20);
        v_count NUMBER;
    BEGIN
        SELECT COUNT(*), MAX(status)
        INTO v_count, v_status
        FROM loans
        WHERE loan_id = p_loan_id;

        IF v_count = 0 THEN
            RETURN 0;  -- 대출 정보 없음
        END IF;

        IF v_status IN ('BORROWED', 'OVERDUE') THEN
            RETURN 1;  -- 반납 가능
        ELSE
            RETURN 0;  -- 반납 불가능
        END IF;

    EXCEPTION
        WHEN OTHERS THEN
            RETURN 0;
    END can_return_book;

END return_package;
/