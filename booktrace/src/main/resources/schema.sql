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
CREATE TABLE libraries (
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
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    FOREIGN KEY (library_id) REFERENCES libraries(library_id)
);

-- 대출 테이블 생성
CREATE TABLE loans (
    loan_id NUMBER PRIMARY KEY,
    user_id NUMBER NOT NULL,
    book_id NUMBER NOT NULL,
    borrow_date DATE NOT NULL,
    extend_number NUMBER DEFAULT 0,
    return_date DATE,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (book_id) REFERENCES books(book_id)
);

-- 예약 테이블 생성
CREATE TABLE reservations (
    resv_id NUMBER PRIMARY KEY,
    user_id NUMBER NOT NULL,
    book_id NUMBER NOT NULL,
    resv_date TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (book_id) REFERENCES books(book_id)
);

-- 독서 로그 테이블 생성
CREATE TABLE reading_log (
    log_id NUMBER PRIMARY KEY,
    user_id NUMBER NOT NULL,
    book_id NUMBER NOT NULL,
    borrow_date DATE NOT NULL,
    return_date DATE,
    mileage NUMBER DEFAULT 0,
    total_mileage NUMBER DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (book_id) REFERENCES books(book_id)
);

-- 주간 인기 도서 VIEW
CREATE OR REPLACE VIEW weekly_popular_books AS
SELECT 
    b.book_id,
    b.title,
    CASE 
        WHEN b.category = '컴퓨터' THEN '#FF6B6B'  -- 빨간색
        WHEN b.category = '소설' THEN '#4ECDC4'    -- 청록색
        WHEN b.category = '과학' THEN '#45B7D1'    -- 하늘색
        WHEN b.category = '역사' THEN '#96CEB4'    -- 연두색
        WHEN b.category = '예술' THEN '#FFEEAD'    -- 노란색
        ELSE '#D3D3D3'                            -- 기본 회색
    END as cover_color
FROM books b
JOIN reading_log rl ON b.book_id = rl.book_id
WHERE rl.borrow_date >= CURRENT_DATE - INTERVAL '7' DAY
GROUP BY b.book_id, b.title, b.category
ORDER BY COUNT(rl.log_id) DESC;

-- 월간 인기 도서 VIEW
CREATE OR REPLACE VIEW monthly_popular_books AS
SELECT 
    b.book_id,
    b.title,
    CASE 
        WHEN b.category = '컴퓨터' THEN '#FF6B6B'  -- 빨간색
        WHEN b.category = '소설' THEN '#4ECDC4'    -- 청록색
        WHEN b.category = '과학' THEN '#45B7D1'    -- 하늘색
        WHEN b.category = '역사' THEN '#96CEB4'    -- 연두색
        WHEN b.category = '예술' THEN '#FFEEAD'    -- 노란색
        ELSE '#D3D3D3'                            -- 기본 회색
    END as cover_color
FROM books b
JOIN reading_log rl ON b.book_id = rl.book_id
WHERE rl.borrow_date >= CURRENT_DATE - INTERVAL '30' DAY
GROUP BY b.book_id, b.title, b.category
ORDER BY COUNT(rl.log_id) DESC;
