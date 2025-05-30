# 📚 BookBank - by PageMates

> 독서 활동을 기록하고, 도서 대출을 관리하며, 나만의 독서 통장을 만들어보세요.

---

## 🧑‍💻 팀 소개 - 북계좌개설팀

- **북계좌개설팀**은 독서와 기술을 융합한 서비스를 만들고자 모인 개발 팀입니다.

---

## 🔍 프로젝트 소개

**BookBank**는 사용자 맞춤형 도서 대출 시스템으로, **대출**, **반납**과 같은 단순 서비스에 더해 **독서 통장**, **인기 도서 통계** 등의 기능을 제공합니다.  
대학 도서관 또는 소규모 기관에서도 사용할 수 있는 독립형 도서 서비스입니다.

---

## 🚀 주요 기능

### 📖 사용자 기능
- 도서 검색 및 대출/반납
- 나의 독서 통장 (기간별 대출 내역, 포인트 관리)
- 인기 도서 TOP10 확인

---

## 🗃 사용 기술

| 구분 | 기술 |
|------|------|
| **Backend** | Java, Spring Boot, JDBC, Oracle PL/SQL |
| **Frontend** | HTML, CSS, JavaScript, React |
| **Database** | Oracle DB |
| **DevOps** | Git, GitHub, SQL Developer, DBeaver |
| **기타** | PL/SQL Procedures, Triggers, Views, Cursors, Functions 등 |

---

## 🧩 데이터베이스 주요 구조

### 📌 핵심 테이블
- `Users`: 사용자 정보
- `Books`: 도서 정보
- `Loans`: 대출/반납 기록
- `ReadingLog`: 리뷰 및 별점
- `Points`: 포인트 및 미션
- `PopularStats`: 인기 도서 통계

### 📌 주요 객체
- **VIEW**: `PopularBooksView` - 인기 책 순위
- **PROCEDURE**: `ReturnAllBooks`, `UpdatePopularBooks`
- **FUNCTION**: `GetBookPopularity`
- **TRIGGER**: `trg_check_duplicate_loan`, `trg_log_loan_activity`
- **CURSOR**: 대출 기록 반복 조회
- **EXCEPTION**: 대출 오류, 무평점 도서 등 예외 처리

---

## 📸 시연 이미지

> 시연 이미지 또는 GIF가 들어갈 수 있는 섹션입니다.  
> 예: 대출 화면, 인기 도서 화면, 독서 통장 UI 등

---

## 🧪 실행 방법

1. Oracle DB에 테이블 및 PL/SQL 객체 생성
2. Spring Boot 프로젝트 실행 (`localhost:8080`)
3. 초기 데이터 삽입 (INSERT 스크립트 제공)
4. 웹 UI 또는 API를 통해 도서 대출/반납 테스트

---

## 🙌 기여자

- 2111465 권재희 (Backend)
- 2115043 김나한 (Frontend)
- 윤서빈 (Backend)

---

