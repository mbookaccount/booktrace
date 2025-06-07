-- 시퀀스 생성
CREATE SEQUENCE user_seq
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

CREATE SEQUENCE book_seq
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

CREATE SEQUENCE library_seq
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

CREATE SEQUENCE loan_seq
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

CREATE SEQUENCE resv_seq
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

CREATE SEQUENCE log_seq
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

CREATE SEQUENCE passbook_seq
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- 도서관 테이블 생성
CREATE TABLE library (
    library_id NUMBER PRIMARY KEY,
    name VARCHAR2(100) NOT NULL
);

-- 사용자 테이블 생성
CREATE TABLE users (
    user_id NUMBER PRIMARY KEY,
    username VARCHAR2(50) NOT NULL UNIQUE,
    id VARCHAR2(50) NOT NULL UNIQUE,
    password VARCHAR2(100) NOT NULL,
    mileage NUMBER DEFAULT 0,
    keywords VARCHAR2(4000),  -- JSON 배열 형태로 저장
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    is_active NUMBER(1) DEFAULT 1 NOT NULL
);

-- 도서 테이블 생성
CREATE TABLE books (
    book_id NUMBER PRIMARY KEY,
    library_id NUMBER NOT NULL,
    title VARCHAR2(200) NOT NULL,
    author VARCHAR2(100) NOT NULL,
    publisher VARCHAR2(100) NOT NULL,
    published_date DATE,
    category VARCHAR2(50),
    available_amount NUMBER DEFAULT 0,
    borrow_count NUMBER DEFAULT 0,  -- 대출 횟수
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    FOREIGN KEY (library_id) REFERENCES library(library_id)
);

-- 대출 테이블 생성
CREATE TABLE loans (
    loan_id NUMBER PRIMARY KEY,
    user_id NUMBER NOT NULL,
    book_id NUMBER NOT NULL,
    borrow_date TIMESTAMP NOT NULL,
    extend_number NUMBER DEFAULT 0,
    return_date TIMESTAMP,
    status VARCHAR2(20) DEFAULT 'NORMAL',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (book_id) REFERENCES books(book_id)
);

-- 예약 테이블 생성
CREATE TABLE reservation (
    resv_id NUMBER PRIMARY KEY,
    user_id NUMBER NOT NULL,
    book_id NUMBER NOT NULL,
    resv_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (book_id) REFERENCES books(book_id)
);

-- 독서 로그 테이블 생성
CREATE TABLE reading_log (
    log_id NUMBER PRIMARY KEY,
    user_id NUMBER NOT NULL,
    book_id NUMBER NOT NULL,
    borrow_date TIMESTAMP NOT NULL,
    return_date TIMESTAMP,
    mileage NUMBER DEFAULT 0,
    total_mileage NUMBER DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (book_id) REFERENCES books(book_id)
);

-- 대출 시 borrow_count 증가 트리거
CREATE OR REPLACE TRIGGER trg_loan_borrow_count
AFTER INSERT ON loans
FOR EACH ROW
BEGIN
    UPDATE books
    SET borrow_count = borrow_count + 1,
        updated_at = SYSTIMESTAMP
    WHERE book_id = :NEW.book_id;
END;
/

-- 반납 시 borrow_count 감소 트리거
CREATE OR REPLACE TRIGGER trg_loan_return_count
AFTER UPDATE OF return_date ON loans
FOR EACH ROW
WHEN (NEW.return_date IS NOT NULL AND OLD.return_date IS NULL)
BEGIN
    UPDATE books
    SET borrow_count = borrow_count - 1,
        updated_at = SYSTIMESTAMP
    WHERE book_id = :NEW.book_id;
END;
/