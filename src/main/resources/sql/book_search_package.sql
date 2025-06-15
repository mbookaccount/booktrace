-- 책 제목으로 검색 (대표 정보 + 보유 도서 목록)
CREATE OR REPLACE PROCEDURE SEARCH_BOOK_DETAIL (
    p_title IN VARCHAR2,
    p_main_book OUT SYS_REFCURSOR,
    p_holding_info OUT SYS_REFCURSOR
)
IS
    v_book_id BOOKS.BOOK_ID%TYPE;
BEGIN
    -- 1. 대표 책 정보 (맨 첫 권 기준으로)
    OPEN p_main_book FOR
        SELECT BOOK_ID, TITLE, AUTHOR, PUBLISHER, PUBLISHED_DATE, COVER_IMAGE
        FROM BOOKS
        WHERE LOWER(TITLE) LIKE '%' || LOWER(TRIM(p_title)) || '%'
        AND ROWNUM = 1;

    -- 2. 해당 제목 전체 보유 도서 목록 (도서관 위치 포함)
    OPEN p_holding_info FOR
        SELECT B.BOOK_ID,
               L.NAME AS LIBRARY_NAME,
               B.CALL_NUMBER,
               CASE WHEN B.AVAILABLE_AMOUNT > 0 THEN 'Y' ELSE 'N' END AS IS_AVAILABLE
        FROM BOOKS B
        JOIN LIBRARIES L ON B.LIBRARY_ID = L.LIBRARY_ID
        WHERE LOWER(B.TITLE) LIKE '%' || LOWER(TRIM(p_title)) || '%'
        ORDER BY B.BOOK_ID;

EXCEPTION
    WHEN OTHERS THEN
        -- 예외 발생 시 빈 결과 반환
        OPEN p_main_book FOR
            SELECT NULL AS BOOK_ID, NULL AS TITLE, NULL AS AUTHOR,
                   NULL AS PUBLISHER, NULL AS PUBLISHED_DATE, NULL AS COVER_IMAGE
            FROM DUAL WHERE 1 = 0;

        OPEN p_holding_info FOR
            SELECT NULL AS BOOK_ID, NULL AS LIBRARY_NAME,
                   NULL AS CALL_NUMBER, NULL AS IS_AVAILABLE
            FROM DUAL WHERE 1 = 0;
END;
/