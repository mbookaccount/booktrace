CREATE OR REPLACE PACKAGE recommendation_pkg AS
    -- 사용자의 선호 카테고리 기반 추천 도서 조회
    PROCEDURE get_recommended_books(
        p_user_id IN NUMBER,
        p_limit IN NUMBER,
        p_books OUT SYS_REFCURSOR
    );
END recommendation_pkg;
/

CREATE OR REPLACE PACKAGE BODY recommendation_pkg AS
    PROCEDURE get_recommended_books(
        p_user_id IN NUMBER,
        p_limit IN NUMBER,
        p_books OUT SYS_REFCURSOR
    ) IS
    BEGIN
        OPEN p_books FOR
            WITH user_categories AS (
                -- 사용자의 선호 카테고리 조회
                SELECT DISTINCT pc.category
                FROM user_preferred_categories pc
                WHERE pc.user_id = p_user_id
            ),
            borrowed_books AS (
                -- 사용자가 현재 대출 중인 도서 조회
                SELECT DISTINCT l.book_id
                FROM loans l
                WHERE l.user_id = p_user_id
                AND l.return_date IS NULL
            )
            SELECT 
                b.book_id,
                NVL(b.title, '제목 없음') as title,
                NVL(b.author, '저자 정보 없음') as author,
                NVL(b.publisher, '출판사 정보 없음') as publisher,
                NVL(b.category, 'UNCATEGORIZED') as category,
                NVL(b.cover_image, '/images/default-cover.jpg') as cover_image,
                NVL(b.available_amount, 0) as available_amount,
                CASE WHEN NVL(b.available_amount, 0) > 0 THEN 1 ELSE 0 END as is_available,
                NVL((SELECT COUNT(*) FROM reservation r WHERE r.book_id = b.book_id), 0) as reservation_count
            FROM books b
            WHERE b.category IN (SELECT category FROM user_categories)
            AND b.book_id NOT IN (SELECT book_id FROM borrowed_books)
            AND NVL(b.available_amount, 0) > 0
            ORDER BY NVL(b.available_amount, 0) DESC, NVL(b.borrow_count, 0) DESC
            FETCH FIRST p_limit ROWS ONLY;
    END get_recommended_books;
END recommendation_pkg;
/
