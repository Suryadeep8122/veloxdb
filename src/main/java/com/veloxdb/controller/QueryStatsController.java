package com.veloxdb.controller;

import com.veloxdb.model.QueryLog;
import com.veloxdb.repository.QueryLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin(origins = "*")
public class QueryStatsController {

    @Autowired
    private QueryLogRepository queryLogRepository;

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("total", queryLogRepository.count());
        summary.put("problematic", queryLogRepository.findByStatus("PROBLEMATIC").size());
        summary.put("slow", queryLogRepository.findByStatus("SLOW").size());
        summary.put("good", queryLogRepository.findByStatus("GOOD").size());
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/top-slow")
    public ResponseEntity<List<QueryLog>> getTopSlow() {
        return ResponseEntity.ok(
                queryLogRepository.findTop10ByOrderByExecutionTimeDesc()
        );
    }
}