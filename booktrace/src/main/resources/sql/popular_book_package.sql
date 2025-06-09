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
    
    -- 카테고리별 색상 반환 함수
    FUNCTION get_category_color(p_category IN VARCHAR2) RETURN VARCHAR2;
END popular_package;
/

CREATE OR REPLACE PACKAGE BODY popular_package AS
    -- 주간 인기 도서 조회
    PROCEDURE get_weekly_popular_books(
        p_books OUT popular_book_cursor
    ) IS
    BEGIN
        OPEN p_books FOR
            SELECT 
                b.book_id, 
                b.title, 
                b.author, 
                b.publisher,
                b.cover_image
                -- popular_package.get_category_color(b.category) as cover_color
            FROM books b
            WHERE b.borrow_count > 0
            ORDER BY b.borrow_count DESC
            FETCH FIRST 10 ROWS ONLY;
    END get_weekly_popular_books;
    
    -- 월간 인기 도서 조회
    PROCEDURE get_monthly_popular_books(
        p_books OUT popular_book_cursor
    ) IS
    BEGIN
        OPEN p_books FOR
            SELECT 
                b.book_id, 
                b.title, 
                b.author, 
                b.publisher,
                b.cover_image
                -- popular_package.get_category_color(b.category) as cover_color
            FROM books b
            WHERE b.borrow_count > 0
            ORDER BY b.borrow_count DESC
            FETCH FIRST 10 ROWS ONLY;
    END get_monthly_popular_books;
END popular_package;
/ 