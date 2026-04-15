package com.veloxdb.service;

import com.veloxdb.repository.QueryLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SlowQueryLogParser {

    @Autowired
    private QueryLogRepository queryLogRepository;

    @Autowired
    private FingerprintService fingerprintService;

    @Autowired
    private ExplainService explainService;

    @Value("${veloxdb.slow-query-log-path:C:/ProgramData/MySQL/MySQL Server 8.0/Data/slow_query.log}")
    private String logFilePath;

    private long lastFilePosition = 0;

    @Scheduled(fixedDelay = 60000)
    public void parseSlowQueryLog() {
        File logFile = new File(logFilePath);
        if (!logFile.exists()) {
            log.warn("Slow query log not found at: {}", logFilePath);
            return;
        }

        try (RandomAccessFile raf = new RandomAccessFile(logFile, "r")) {
            raf.seek(lastFilePosition);
            List<String> queries = new ArrayList<>();
            StringBuilder currentQuery = new StringBuilder();
            String line;

            while ((line = raf.readLine()) != null) {
                if (line.startsWith("#")) {
                    if (currentQuery.length() > 0) {
                        queries.add(currentQuery.toString().trim());
                        currentQuery = new StringBuilder();
                    }
                } else if (!line.isEmpty() && !line.startsWith("SET ")
                        && !line.startsWith("use ")) {
                    currentQuery.append(line).append(" ");
                }
            }

            if (currentQuery.length() > 0) {
                queries.add(currentQuery.toString().trim());
            }

            lastFilePosition = raf.getFilePointer();

            for (String query : queries) {
                if (query.toUpperCase().startsWith("SELECT") ||
                        query.toUpperCase().startsWith("UPDATE") ||
                        query.toUpperCase().startsWith("DELETE")) {
                    try {
                        explainService.explainAndAnalyze(query);
                        log.info("Auto-analyzed: {}",
                                query.substring(0, Math.min(50, query.length())));
                    } catch (Exception e) {
                        log.error("Failed to analyze: {}", e.getMessage());
                    }
                }
            }

        } catch (IOException e) {
            log.error("Error reading slow query log: {}", e.getMessage());
        }
    }
}