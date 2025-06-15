CREATE OR REPLACE PACKAGE BODY recommendation_pkg AS
    PROCEDURE get_recommended_books(
        p_user_id IN NUMBER,
        p_limit IN NUMBER,
        p_books OUT SYS_REFCURSOR
    ) IS
    BEGIN
        OPEN p_books FOR
            SELECT * FROM (
                WITH user_categories AS (
                    SELECT DISTINCT pc.category
                    FROM user_preferred_categories pc
                    WHERE pc.user_id = p_user_id
                ),
                borrowed_books AS (
                    SELECT DISTINCT l.book_id
                    FROM loans l
                    WHERE l.user_id = p_user_id
                    AND l.return_date IS NULL
                )
                SELECT
                    b.book_id,
                    b.title,
                    b.category,
                    b.cover_image,
                    CASE WHEN b.available_amount > 0 THEN 1 ELSE 0 END as is_available,
                    (SELECT COUNT(*) FROM reservations r WHERE r.book_id = b.book_id) as reservation_count
                FROM books b
                WHERE b.category IN (SELECT category FROM user_categories)
                AND b.book_id NOT IN (SELECT book_id FROM borrowed_books)
                AND b.available_amount > 0
                ORDER BY b.available_amount DESC, b.borrow_count DESC
            )
            WHERE ROWNUM <= p_limit;
    END get_recommended_books;
END recommendation_pkg;
/
