-- 로그인 ID 중복 확인 함수
CREATE OR REPLACE FUNCTION CHECK_LOGIN_ID_EXISTS(p_login_id VARCHAR2)
RETURN NUMBER
IS
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count
    FROM USERS
    WHERE LOGIN_ID = p_login_id;

    RETURN v_count;

EXCEPTION
    WHEN OTHERS THEN
        RETURN -1;
END;
/

-- 회원가입 프로시저
CREATE OR REPLACE PROCEDURE REGISTER_USER(
    p_user_name VARCHAR2,
    p_login_id VARCHAR2,
    p_password VARCHAR2,
    p_password_confirm VARCHAR2,
    p_preferred_categories VARCHAR2,
    p_result OUT NUMBER,
    p_message OUT VARCHAR2,
    p_user_id OUT NUMBER
)
IS
    v_new_user_id NUMBER;
    v_category VARCHAR2(30);
    v_start_pos NUMBER := 1;
    v_comma_pos NUMBER;
    v_category_count NUMBER := 0;
BEGIN
    -- 1. 비밀번호 확인
    IF p_password != p_password_confirm THEN
        p_result := 0;
        p_message := '비밀번호가 일치하지 않습니다.';
        p_user_id := NULL;
        RETURN;
    END IF;

    -- 2. 선호 카테고리 비어있는지 확인
    IF p_preferred_categories IS NULL OR LENGTH(TRIM(p_preferred_categories)) = 0 THEN
        p_result := 0;
        p_message := '선호 카테고리를 입력해주세요.';
        p_user_id := NULL;
        RETURN;
    END IF;

    -- 3. USER_ID 생성
    SELECT user_seq.NEXTVAL INTO v_new_user_id FROM dual;

    INSERT INTO USERS (USER_ID, USER_NAME, LOGIN_ID, PASSWORD, MILEAGE, IS_ACTIVE)
    VALUES (v_new_user_id, p_user_name, p_login_id, p_password, 0, 'Y');

    -- 4. 선호 카테고리 삽입
    v_start_pos := 1;
    LOOP
        v_comma_pos := INSTR(p_preferred_categories, ',', v_start_pos);

        IF v_comma_pos = 0 THEN
            v_category := TRIM(SUBSTR(p_preferred_categories, v_start_pos));
        ELSE
            v_category := TRIM(SUBSTR(p_preferred_categories, v_start_pos, v_comma_pos - v_start_pos));
        END IF;

        IF LENGTH(v_category) > 0 THEN
            INSERT INTO USER_PREFERRED_CATEGORIES (USER_ID, CATEGORY)
            VALUES (v_new_user_id, v_category);
            v_category_count := v_category_count + 1;
        END IF;

        EXIT WHEN v_comma_pos = 0;
        v_start_pos := v_comma_pos + 1;
    END LOOP;

    -- 5. 완료 메시지
    p_result := 1;
    p_message := '회원가입 완료! 선호 카테고리 ' || v_category_count || '개 등록';
    p_user_id := v_new_user_id;

    COMMIT;

EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
        ROLLBACK;
        p_result := 0;
        p_message := '중복된 로그인 ID입니다.';
        p_user_id := NULL;
    WHEN OTHERS THEN
        ROLLBACK;
        p_result := 0;
        p_message := '회원가입 중 오류: ' || SQLERRM;
        p_user_id := NULL;
END;
/

-- 단순 로그인 프로시저
CREATE OR REPLACE PROCEDURE LOGIN_USER_SIMPLE (
    p_login_id IN VARCHAR2,
    p_password IN VARCHAR2,
    p_user_id OUT NUMBER
)
IS
    v_password USERS.PASSWORD%TYPE;
BEGIN
    SELECT PASSWORD, USER_ID
    INTO v_password, p_user_id
    FROM USERS
    WHERE LOGIN_ID = p_login_id;

    IF p_password != v_password THEN
        p_user_id := NULL;
    END IF;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        p_user_id := NULL;
    WHEN OTHERS THEN
        p_user_id := NULL;
END;
/
