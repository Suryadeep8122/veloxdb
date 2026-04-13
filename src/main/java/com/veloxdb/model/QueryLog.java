package com.veloxdb.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "query_logs")
@Data
public class QueryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String queryText;

    private Double executionTime;

    private String status; // GOOD, SLOW, PROBLEMATIC

    private String fingerprint;

    private LocalDateTime capturedAt;

    @PrePersist
    public void prePersist() {
        capturedAt = LocalDateTime.now();
    }
}