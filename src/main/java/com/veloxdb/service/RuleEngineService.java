package com.veloxdb.service;

import com.veloxdb.model.AnalysisResult;
import com.veloxdb.model.QueryLog;
import org.springframework.stereotype.Service;

@Service
public class RuleEngineService {

    public AnalysisResult analyze(QueryLog queryLog, String scanType,
                                  String indexUsed, Long rowsExamined) {
        AnalysisResult result = new AnalysisResult();
        result.setQueryLog(queryLog);
        result.setScanType(scanType);
        result.setIndexUsed(indexUsed);
        result.setRowsExamined(rowsExamined);

        // Rule 1: Full table scan
        if ("ALL".equalsIgnoreCase(scanType)) {
            result.setClassification("PROBLEMATIC");
            result.setExplanation("Full table scan detected. No index is being used.");
            result.setSuggestion(generateIndexSuggestion(queryLog.getQueryText()));
            return result;
        }

        // Rule 2: No index used
        if (indexUsed == null || indexUsed.equalsIgnoreCase("NULL")) {
            result.setClassification("SLOW");
            result.setExplanation("No index used on this query.");
            result.setSuggestion(generateIndexSuggestion(queryLog.getQueryText()));
            return result;
        }

        // Rule 3: Too many rows examined
        if (rowsExamined != null && rowsExamined > 5000) {
            result.setClassification("SLOW");
            result.setExplanation("Query examined " + rowsExamined + " rows. Consider adding filters.");
            result.setSuggestion("Add a WHERE clause to reduce rows examined. Consider pagination.");
            return result;
        }

        // Rule 4: SELECT * detected
        if (queryLog.getQueryText().toUpperCase().contains("SELECT *")) {
            result.setClassification("SLOW");
            result.setExplanation("SELECT * fetches all columns. Specify only needed columns.");
            result.setSuggestion("Replace SELECT * with specific column names: SELECT id, name, email ...");
            return result;
        }

        // Rule 5: Good query
        result.setClassification("GOOD");
        result.setExplanation("Query is using index and examining minimal rows.");
        result.setSuggestion("No optimization needed.");
        return result;
    }

    private String generateIndexSuggestion(String queryText) {
        if (queryText == null) return "Analyze query and add appropriate index.";

        String upper = queryText.toUpperCase();

        // Extract table name hint
        String tableName = "your_table";
        if (upper.contains("FROM")) {
            try {
                String afterFrom = queryText.substring(upper.indexOf("FROM") + 5).trim();
                tableName = afterFrom.split("\\s+")[0].replaceAll("[^a-zA-Z0-9_]", "");
            } catch (Exception e) {
                tableName = "your_table";
            }
        }

        // Extract WHERE column hint
        String columnName = "your_column";
        if (upper.contains("WHERE")) {
            try {
                String afterWhere = queryText.substring(upper.indexOf("WHERE") + 6).trim();
                columnName = afterWhere.split("[\\s=<>!]+")[0].replaceAll("[^a-zA-Z0-9_]", "");
            } catch (Exception e) {
                columnName = "your_column";
            }
        }

        return "CREATE INDEX idx_" + tableName + "_" + columnName +
                " ON " + tableName + "(" + columnName + ");";
    }
}