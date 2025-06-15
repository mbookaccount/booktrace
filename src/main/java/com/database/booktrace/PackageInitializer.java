package com.database.booktrace;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Scanner;
import java.util.regex.Pattern;

@Component
public class PackageInitializer {

    private final DataSource dataSource;
    private static final Pattern PLSQL_BLOCK_PATTERN = Pattern.compile(
            "(?i)\\s*(CREATE\\s+(OR\\s+REPLACE\\s+)?(PACKAGE|PROCEDURE|FUNCTION|TRIGGER)|BEGIN|DECLARE)",
            Pattern.CASE_INSENSITIVE
    );

    public PackageInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void initializePackages() throws Exception {
        try {
            runScript("sql/loan_package.sql");
            runScript("sql/popular_package.sql");
            runScript("sql/popular_book_package.sql");
            runScript("sql/reading_log_package.sql");
            runScript("sql/triggers.sql");
            runScript("sql/book_search_package.sql");
            runScript("sql/user_auth_package.sql");
            runScript("sql/recommendation_package.sql");
            runScript("sql/return_package.sql");
            // 초기 데이터 삽입 추가
            // runScript("sql/initial_data_insert.sql");
            System.out.println(" 모든 패키지 초기화가 완료되었습니다.");
        } catch (Exception e) {
            System.err.println(" 패키지 초기화 중 오류가 발생했습니다: " + e.getMessage());
            throw e;
        }
    }

    private void runScript(String path) throws Exception {
        Resource resource = new ClassPathResource(path);
        try (Connection conn = dataSource.getConnection();
             Scanner scanner = new Scanner(resource.getInputStream(), StandardCharsets.UTF_8)) {

            StringBuilder sqlBuilder = new StringBuilder();
            int lineNumber = 0;
            boolean inPlSqlBlock = false;

            System.out.println( path + " 파일 실행 중...");

            while (scanner.hasNextLine()) {
                lineNumber++;
                String line = scanner.nextLine();
                String trimmedLine = line.trim();

                // 주석이나 빈 줄은 건너뛰기
                if (trimmedLine.startsWith("--") || trimmedLine.isEmpty()) {
                    continue;
                }

                // SQL 문 추가
                sqlBuilder.append(line).append("\n");

                // PL/SQL 블록 시작 감지
                if (!inPlSqlBlock && PLSQL_BLOCK_PATTERN.matcher(trimmedLine).find()) {
                    inPlSqlBlock = true;
                }

                // "/" 구분자를 만나면 PL/SQL 블록 실행
                if (trimmedLine.equals("/")) {
                    if (sqlBuilder.length() > 0) {
                        String sql = sqlBuilder.toString().trim();
                        if (!sql.isEmpty()) {
                            // "/" 제거
                            sql = sql.substring(0, sql.lastIndexOf("/")).trim();
                            executeSQL(conn, sql, path, lineNumber, true);
                        }
                        sqlBuilder.setLength(0);
                        inPlSqlBlock = false;
                    }
                    continue;
                }

                // 세미콜론으로 끝나는 일반 SQL 문 처리
                if (!inPlSqlBlock && trimmedLine.endsWith(";")) {
                    String sql = sqlBuilder.toString().trim();
                    if (!sql.isEmpty()) {
                        // 세미콜론 제거
                        if (sql.endsWith(";")) {
                            sql = sql.substring(0, sql.length() - 1);
                        }
                        executeSQL(conn, sql, path, lineNumber, false);
                        sqlBuilder.setLength(0);
                    }
                }
            }

            // 파일 끝에 남은 SQL이 있으면 실행
            if (sqlBuilder.length() > 0) {
                String sql = sqlBuilder.toString().trim();
                if (!sql.isEmpty()) {
                    if (sql.endsWith(";")) {
                        sql = sql.substring(0, sql.length() - 1);
                    }
                    executeSQL(conn, sql, path, lineNumber, inPlSqlBlock);
                }
            }

            System.out.println(path + " 실행 완료");
        }
    }

    private void executeSQL(Connection conn, String sql, String fileName, int lineNumber, boolean isPlSql) throws Exception {
        if (sql.trim().isEmpty()) {
            return;
        }

        try (Statement stmt = conn.createStatement()) {
            // SQL 미리보기 출력 (처음 150자만)
            String preview = sql.length() > 150 ? sql.substring(0, 150) + "..." : sql;
            System.out.println("SQL 실행: " + preview.replaceAll("\\s+", " "));

            // SQL 실행
            stmt.execute(sql);
            System.out.println("SQL 실행 성공");

        } catch (Exception e) {
            System.err.println(" SQL 실행 실패");
            System.err.println(" 파일: " + fileName);
            System.err.println(" 라인 근처: " + lineNumber);
            System.err.println("PL/SQL 블록: " + (isPlSql ? "예" : "아니오"));
            System.err.println(" 실행하려던 SQL:");
            System.err.println("─".repeat(50));
            System.err.println(sql);
            System.err.println("─".repeat(50));
            System.err.println("오류 메시지: " + e.getMessage());

            // Oracle 특정 오류 코드에 대한 힌트 제공
            if (e.getMessage().contains("ORA-00911")) {
                System.err.println("SQL 문에 잘못된 문자가 있습니다. 세미콜론(;)이나 슬래시(/)를 확인해주세요.");
            } else if (e.getMessage().contains("ORA-00942")) {
                System.err.println("테이블이나 뷰가 존재하지 않습니다.");
            } else if (e.getMessage().contains("ORA-00955")) {
                System.err.println(" 객체가 이미 존재합니다. CREATE OR REPLACE를 사용하세요.");
            }

            throw e;
        }
    }
}