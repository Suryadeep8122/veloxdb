
package com.veloxdb.controller;

import com.veloxdb.model.AnalysisResult;
import com.veloxdb.model.QueryLog;
import com.veloxdb.repository.AnalysisResultRepository;
import com.veloxdb.repository.QueryLogRepository;
import com.veloxdb.service.ExplainService;
import com.veloxdb.service.RuleEngineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class QueryController {

    @Autowired
    private QueryLogRepository queryLogRepository;

    @Autowired
    private AnalysisResultRepository analysisResultRepository;

    @Autowired
    private RuleEngineService ruleEngineService;

    @Autowired
    private ExplainService explainService;

    // GET all captured queries
    @GetMapping("/queries")
    public ResponseEntity<List<QueryLog>> getAllQueries() {
        return ResponseEntity.ok(queryLogRepository.findAll());
    }

    // GET top 10 slowest queries
    @GetMapping("/queries/top-slow")
    public ResponseEntity<List<QueryLog>> getTopSlow() {
        return ResponseEntity.ok(queryLogRepository.findTop10ByOrderByExecutionTimeDesc());
    }

    // POST analyze a query manually
    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResult> analyzeQuery(@RequestBody Map<String, String> request) {
        String queryText = request.get("query");
        String scanType = request.getOrDefault("scanType", "ALL");
        String indexUsed = request.getOrDefault("indexUsed", "NULL");
        Long rowsExamined = Long.parseLong(request.getOrDefault("rowsExamined", "0"));

        // Save query log
        QueryLog queryLog = new QueryLog();
        queryLog.setQueryText(queryText);
        queryLog.setExecutionTime(0.0);
        queryLog.setFingerprint(queryText.replaceAll("\\d+", "?").toLowerCase().trim());
        queryLog = queryLogRepository.save(queryLog);

        // Run rule engine
        AnalysisResult result = ruleEngineService.analyze(queryLog, scanType, indexUsed, rowsExamined);
        queryLog.setStatus(result.getClassification());
        queryLogRepository.save(queryLog);

        // Save and return result
        return ResponseEntity.ok(analysisResultRepository.save(result));
    }

    // GET all analysis results
    @GetMapping("/results")
    public ResponseEntity<List<AnalysisResult>> getAllResults() {
        return ResponseEntity.ok(analysisResultRepository.findAll());
    }
    // POST explain a real query against MySQL
    @PostMapping("/explain")
    public ResponseEntity<AnalysisResult> explainQuery(@RequestBody Map<String, String> request) {
        String queryText = request.get("query");
        AnalysisResult result = explainService.explainAndAnalyze(queryText);
        return ResponseEntity.ok(result);
    }
}