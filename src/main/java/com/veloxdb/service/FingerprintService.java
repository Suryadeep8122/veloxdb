package com.veloxdb.service;

import org.springframework.stereotype.Service;

@Service
public class FingerprintService {

    public String generate(String queryText) {
        if (queryText == null) return "";

        return queryText
                .toLowerCase()
                .trim()
                // Replace all string literals with ?
                .replaceAll("'[^']*'", "?")
                // Replace all numbers with ?
                .replaceAll("\\b\\d+\\b", "?")
                // Replace multiple spaces with single space
                .replaceAll("\\s+", " ")
                // Remove trailing semicolon
                .replaceAll(";$", "")
                .trim();
    }

    public boolean isSimilar(String query1, String query2) {
        return generate(query1).equals(generate(query2));
    }
}