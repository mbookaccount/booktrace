-- book 테이블에 status 컬럼 추가
--ALTER TABLE books ADD status VARCHAR2(20) DEFAULT 'AVAILABLE' NOT NULL;
--/
-- 대출 상태 변경 트리거
CREATE OR REPLACE TRIGGER book_loan_status_trigger
AFTER INSERT ON LOANS
FOR EACH ROW
BEGIN
    -- 대출 시 도서 상태를 'LOANED'로 변경
    UPDATE books
    SET status = 'LOANED'
    WHERE book_id = :NEW.book_id;
END;
/

-- 반납 시 도서 상태를 'AVAILABLE'로 변경하는 트리거
CREATE OR REPLACE TRIGGER book_return_status_trigger
AFTER UPDATE OF return_date ON LOANS
FOR EACH ROW
WHEN (NEW.return_date IS NOT NULL AND OLD.return_date IS NULL)
BEGIN
    -- 반납 시 도서 상태를 'AVAILABLE'로 변경
    UPDATE books
    SET status = 'AVAILABLE'
    WHERE book_id = :NEW.book_id;
END;
/ 