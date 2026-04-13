package com.veloxdb.repository;

import com.veloxdb.model.QueryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QueryLogRepository extends JpaRepository<QueryLog, Long> {

    List<QueryLog> findByStatus(String status);

    List<QueryLog> findByFingerprint(String fingerprint);

    List<QueryLog> findTop10ByOrderByExecutionTimeDesc();
}