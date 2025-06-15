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
        v_limit NUMBER := NVL(p_limit, 10);
    BEGIN
        -- 사용자 선호 카테고리 기반 추천 (ROWNUM 사용)
        OPEN p_books FOR
            SELECT * FROM (
                SELECT
                    b.book_id,
                    NVL(b.title, '제목 없음') as title,
                    NVL(b.author, '저자 정보 없음') as author,
                    NVL(b.publisher, '출판사 정보 없음') as publisher,
                    NVL(b.category, 'UNCATEGORIZED') as category,
                    NVL(b.cover_image, '/images/default-cover.jpg') as cover_image,
                    NVL(b.available_amount, 0) as available_amount,
                    CASE WHEN NVL(b.available_amount, 0) > 0 THEN 1 ELSE 0 END as is_available,
                    (SELECT COUNT(*) FROM reservations r WHERE r.book_id = b.book_id) as reservation_count
                FROM books b
                WHERE b.category IN (
                    SELECT DISTINCT pc.category
                    FROM user_preferred_categories pc
                    WHERE pc.user_id = p_user_id
                )
                AND b.book_id NOT IN (
                    SELECT DISTINCT l.book_id
                    FROM loans l
                    WHERE l.user_id = p_user_id
                    AND l.status IN ('BORROWED', 'OVERDUE')
                )
                AND NVL(b.available_amount, 0) > 0
                ORDER BY NVL(b.available_amount, 0) DESC, NVL(b.borrow_count, 0) DESC
            )
            WHERE ROWNUM <= v_limit;

    EXCEPTION
        WHEN OTHERS THEN
            -- 오류 시 랜덤 추천
            OPEN p_books FOR
                SELECT * FROM (
                    SELECT
                        book_id,
                        NVL(title, '제목 없음') as title,
                        NVL(author, '저자 정보 없음') as author,
                        NVL(publisher, '출판사 정보 없음') as publisher,
                        NVL(category, 'UNCATEGORIZED') as category,
                        NVL(cover_image, '/images/default-cover.jpg') as cover_image,
                        NVL(available_amount, 0) as available_amount,
                        1 as is_available,
                        0 as reservation_count
                    FROM books
                    WHERE available_amount > 0
                    ORDER BY DBMS_RANDOM.VALUE
                )
                WHERE ROWNUM <= 5;
    END get_recommended_books;
END recommendation_pkg;
/