package com.veloxdb.service;

import com.veloxdb.model.AnalysisResult;
import com.veloxdb.model.QueryLog;
import com.veloxdb.repository.AnalysisResultRepository;
import com.veloxdb.repository.QueryLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.*;

@Service
@Slf4j
public class ExplainService {
    @Autowired
    private FingerprintService fingerprintService;

    @Autowired
    private RuleEngineService ruleEngineService;

    @Autowired
    private QueryLogRepository queryLogRepository;

    @Autowired
    private AnalysisResultRepository analysisResultRepository;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    public AnalysisResult explainAndAnalyze(String queryText) {
        // Save query log first
        QueryLog queryLog = new QueryLog();
        queryLog.setQueryText(queryText);
        queryLog.setFingerprint(fingerprintService.generate(queryText));

        String scanType = "UNKNOWN";
        String indexUsed = "NULL";
        long rowsExamined = 0;
        double executionTime = 0.0;

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             Statement stmt = conn.createStatement()) {

            long startTime = System.currentTimeMillis();

            // Run EXPLAIN
            ResultSet rs = stmt.executeQuery("EXPLAIN " + queryText);

            long endTime = System.currentTimeMillis();
            executionTime = (endTime - startTime) / 1000.0;

            if (rs.next()) {
                scanType = rs.getString("type") != null ? rs.getString("type") : "ALL";
                indexUsed = rs.getString("key");
                Object rows = rs.getObject("rows");
                rowsExamined = rows != null ? Long.parseLong(rows.toString()) : 0;
            }

            rs.close();
            log.info("EXPLAIN executed: type={}, key={}, rows={}", scanType, indexUsed, rowsExamined);

        } catch (SQLException e) {
            log.error("EXPLAIN failed for query: {}", queryText, e);
            // Still analyze with defaults if EXPLAIN fails
            scanType = "ALL";
        }

        queryLog.setExecutionTime(executionTime);
        queryLog = queryLogRepository.save(queryLog);

        // Run rule engine with real EXPLAIN data
        AnalysisResult result = ruleEngineService.analyze(queryLog, scanType, indexUsed, rowsExamined);
        queryLog.setStatus(result.getClassification());
        queryLogRepository.save(queryLog);

        return analysisResultRepository.save(result);
    }
}