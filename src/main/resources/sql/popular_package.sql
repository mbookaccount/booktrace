
-- 주간 인기도서 View
CREATE OR REPLACE VIEW weekly_popular_books_view AS
SELECT * FROM (
    SELECT
        b.book_id,
        b.title,
        b.cover_image,
        COUNT(l.loan_id) as borrow_count
    FROM books b
    JOIN loans l ON b.book_id = l.book_id
    WHERE l.borrow_date >= TRUNC(SYSDATE) - 7  -- 최근 7일
    GROUP BY b.book_id, b.title, b.cover_image
    ORDER BY borrow_count DESC
)
WHERE ROWNUM <= 10;
/

-- 월간 인기도서 View
CREATE OR REPLACE VIEW monthly_popular_books_view AS
SELECT * FROM (
    SELECT
        b.book_id,
        b.title,
        b.cover_image,
        COUNT(l.loan_id) as borrow_count
    FROM books b
    JOIN loans l ON b.book_id = l.book_id
    WHERE l.borrow_date >= TRUNC(SYSDATE) - 30  -- 최근 30일
    GROUP BY b.book_id, b.title, b.cover_image
    ORDER BY borrow_count DESC
)
WHERE ROWNUM <= 10;
/

-- 인기도서 패키지
CREATE OR REPLACE PACKAGE popular_package AS
    -- 커서 타입 정의
    TYPE popular_book_cursor IS REF CURSOR;

    -- 주간 인기 도서 조회
    PROCEDURE get_weekly_popular_books(
        p_books OUT popular_book_cursor
    );

    -- 월간 인기 도서 조회
    PROCEDURE get_monthly_popular_books(
        p_books OUT popular_book_cursor
    );
END popular_package;
/

CREATE OR REPLACE PACKAGE BODY popular_package AS
    -- 주간 인기 도서 조회
    PROCEDURE get_weekly_popular_books(
        p_books OUT popular_book_cursor
    ) IS
    BEGIN
        OPEN p_books FOR
            SELECT book_id, title, cover_image FROM weekly_popular_books_view;
    END get_weekly_popular_books;

    -- 월간 인기 도서 조회
    PROCEDURE get_monthly_popular_books(
        p_books OUT popular_book_cursor
    ) IS
    BEGIN
        OPEN p_books FOR
            SELECT book_id, title, cover_image FROM monthly_popular_books_view;
    END get_monthly_popular_books;
END popular_package;
/
