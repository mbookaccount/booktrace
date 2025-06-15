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

@Component
public class PackageInitializer {

    private final DataSource dataSource;

    public PackageInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void initializePackages() throws Exception {
        runScript("sql/loan_package.sql");
        runScript("sql/popular_package.sql");
        runScript("sql/popular_book_package.sql");
        runScript("sql/reading_log_package.sql");
        runScript("sql/triggers.sql");

        // 초기 데이터 삽입 추가
       // runScript("sql/initial_data_insert.sql");
    }

    private void runScript(String path) throws Exception {
        Resource resource = new ClassPathResource(path);
        try (Connection conn = dataSource.getConnection();
             Scanner scanner = new Scanner(resource.getInputStream(), StandardCharsets.UTF_8);
             Statement stmt = conn.createStatement()) {

            StringBuilder sqlBuilder = new StringBuilder();
            int lineNumber = 0;

            System.out.println("=== " + path + " 실행 시작 ===");

            while (scanner.hasNextLine()) {
                lineNumber++;
                String line = scanner.nextLine().trim();

                // 주석이나 빈 줄은 건너뛰기
                if (line.startsWith("--") || line.isBlank()) {
                    continue;
                }

                // "/" 구분자를 만나면 SQL 실행 (PL/SQL용)
                if (line.equals("/")) {
                    if (sqlBuilder.length() > 0) {
                        executeSQL(stmt, sqlBuilder.toString(), path, lineNumber);
                        sqlBuilder.setLength(0);
                    }
                    continue;
                }

                sqlBuilder.append(line).append("\n");
            }

            // 파일 끝에 남은 SQL이 있으면 실행
            if (sqlBuilder.length() > 0) {
                executeSQL(stmt, sqlBuilder.toString(), path, lineNumber);
            }

            System.out.println("=== " + path + " 실행 완료 ===");
        }
    }

    private void executeSQL(Statement stmt, String sql, String fileName, int lineNumber) throws Exception {
        String trimmedSql = sql.trim();
        if (!trimmedSql.isEmpty()) {
            try {
                System.out.println("실행할 SQL: " + trimmedSql.substring(0, Math.min(100, trimmedSql.length())) + "...");
                stmt.execute(trimmedSql);
                System.out.println("SQL 실행 성공");
            } catch (Exception e) {
                System.err.println("=== SQL 실행 실패 ===");
                System.err.println("파일: " + fileName + ", 라인 근처: " + lineNumber);
                System.err.println("실행하려던 SQL: " + trimmedSql);
                System.err.println("오류 메시지: " + e.getMessage());
                System.err.println("=====================");
                throw e;
            }
        }
    }
}