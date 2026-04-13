package com.veloxdb.repository;

import com.veloxdb.model.AnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {

    List<AnalysisResult> findByClassification(String classification);

    List<AnalysisResult> findByQueryLog_Id(Long queryLogId);
}