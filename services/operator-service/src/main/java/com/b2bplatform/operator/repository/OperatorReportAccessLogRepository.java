package com.b2bplatform.operator.repository;

import com.b2bplatform.operator.model.OperatorReportAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OperatorReportAccessLogRepository extends JpaRepository<OperatorReportAccessLog, Long> {
    
    List<OperatorReportAccessLog> findByOperatorIdOrderByAccessedAtDesc(Long operatorId);
    
    List<OperatorReportAccessLog> findByUserIdentifierOrderByAccessedAtDesc(String userIdentifier);
    
    List<OperatorReportAccessLog> findByOperatorIdAndAccessedAtBetweenOrderByAccessedAtDesc(
        Long operatorId, LocalDateTime startDate, LocalDateTime endDate);
    
    List<OperatorReportAccessLog> findByReportTypeOrderByAccessedAtDesc(String reportType);
}
