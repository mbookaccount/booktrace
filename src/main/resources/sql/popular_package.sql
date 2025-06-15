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
            SELECT * FROM (
                SELECT
                    b.book_id,
                    b.title,
                    b.author,
                    b.publisher,
                    b.cover_image,
                    COUNT(l.log_id) as borrow_count
                FROM books b
                JOIN loan l ON b.book_id = l.book_id
                WHERE l.borrow_date >= TRUNC(SYSDATE) - 7  -- 최근 7일
                GROUP BY b.book_id, b.title, b.author, b.publisher, b.cover_image
                ORDER BY borrow_count DESC
            )
            WHERE ROWNUM <= 10;
    END get_weekly_popular_books;

    -- 월간 인기 도서 조회
    PROCEDURE get_monthly_popular_books(
        p_books OUT popular_book_cursor
    ) IS
    BEGIN
        OPEN p_books FOR
            SELECT * FROM (
                SELECT
                    b.book_id,
                    b.title,
                    b.author,
                    b.publisher,
                    b.cover_image,
                    COUNT(l.log_id) as borrow_count
                FROM books b
                JOIN loan l ON b.book_id = l.book_id
                WHERE l.borrow_date >= TRUNC(SYSDATE) - 30  -- 최근 30일
                GROUP BY b.book_id, b.title, b.author, b.publisher, b.cover_image
                ORDER BY borrow_count DESC
            )
            WHERE ROWNUM <= 10;
    END get_monthly_popular_books;
END popular_package;
/