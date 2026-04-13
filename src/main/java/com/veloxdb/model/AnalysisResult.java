package com.veloxdb.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_results")
@Data
public class AnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "query_log_id")
    private QueryLog queryLog;

    private String scanType;       // ALL, ref, eq_ref, index

    private String indexUsed;      // NULL or index name

    private Long rowsExamined;

    private String classification; // GOOD, SLOW, PROBLEMATIC

    @Column(columnDefinition = "TEXT")
    private String suggestion;     // exact fix SQL

    @Column(columnDefinition = "TEXT")
    private String explanation;    // human readable reason

    private LocalDateTime analyzedAt;

    @PrePersist
    public void prePersist() {
        analyzedAt = LocalDateTime.now();
    }
}