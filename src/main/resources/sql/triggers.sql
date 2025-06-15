-- 대출 시 가용 수량 감소 트리거
CREATE OR REPLACE TRIGGER TRG_BOOK_BORROW
    AFTER INSERT ON LOANS
    FOR EACH ROW
BEGIN
    UPDATE BOOKS
    SET AVAILABLE_AMOUNT = AVAILABLE_AMOUNT - 1
    WHERE BOOK_ID = :NEW.BOOK_ID;

    DBMS_OUTPUT.PUT_LINE('책 대출됨 - 가용수량 감소');
END;
/

-- 반납 시 가용 수량 증가 트리거
CREATE OR REPLACE TRIGGER TRG_BOOK_RETURN
    AFTER UPDATE ON LOANS
    FOR EACH ROW
    WHEN (OLD.STATUS = 'BORROWED' AND NEW.STATUS = 'RETURNED')
BEGIN
    UPDATE BOOKS
    SET AVAILABLE_AMOUNT = AVAILABLE_AMOUNT + 1
    WHERE BOOK_ID = :NEW.BOOK_ID;

    DBMS_OUTPUT.PUT_LINE('책 반납됨 - 가용수량 증가');
END;
/

-- 시퀀스 트리거 추가
CREATE OR REPLACE TRIGGER TRG_LOANS_ID
    BEFORE INSERT ON LOANS
    FOR EACH ROW
BEGIN
    :NEW.LOAN_ID := loan_seq.NEXTVAL;
END;
/

--예약 트리거 추가
CREATE OR REPLACE TRIGGER TRG_RESV_ID
BEFORE INSERT ON RESERVATIONS
FOR EACH ROW
BEGIN
    IF :NEW.RESV_ID IS NULL THEN
        :NEW.RESV_ID := resv_seq.NEXTVAL;
    END IF;
END;
/

CREATE OR REPLACE TRIGGER TRG_INSERT_READING_LOG
    AFTER UPDATE ON LOANS
    FOR EACH ROW
    WHEN (OLD.STATUS = 'BORROWED' AND NEW.STATUS = 'RETURNED')
DECLARE
    v_mileage NUMBER;
    v_current_total_mileage NUMBER;
    v_cutoff_time TIMESTAMP := TO_TIMESTAMP('2025-06-16 08:00:00', 'YYYY-MM-DD HH24:MI:SS');
BEGIN
    -- 특정 시간 이후 반납된 것만 처리
    IF :NEW.updated_at < v_cutoff_time THEN
        RETURN; -- 조기 종료, 독서통장에 기록하지 않음
    END IF;

    -- 마일리지 계산 (정시 반납: 1점, 연체 시 0점)
    IF :NEW.updated_at <= :NEW.return_date THEN
        v_mileage := 1;
    ELSE
        v_mileage := 0;
    END IF;

    -- 현재 사용자의 총 마일리지 조회
    SELECT COALESCE(mileage, 0) INTO v_current_total_mileage
    FROM users WHERE user_id = :NEW.user_id;

    -- users 테이블 마일리지 증가
    UPDATE users
    SET mileage = mileage + v_mileage,
        updated_at = SYSTIMESTAMP
    WHERE user_id = :NEW.user_id;

    -- reading_log 테이블에 반납 기록 삽입
    INSERT INTO reading_log (
        log_id,
        user_id,
        book_id,
        borrow_date,
        return_date,
        mileage,
        total_mileage,
        created_at,
        updated_at
    ) VALUES (
        LOG_SEQ.NEXTVAL,
        :NEW.user_id,
        :NEW.book_id,
        :NEW.borrow_date,
        :NEW.updated_at,
        v_mileage,
        v_current_total_mileage + v_mileage,
        SYSTIMESTAMP,
        SYSTIMESTAMP
    );

    DBMS_OUTPUT.PUT_LINE('독서통장 기록 추가 + 마일리지 증가: +' || v_mileage);

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('독서통장/마일리지 처리 오류: ' || SQLERRM);
END;
/