package com.b2bplatform.operator.repository;

import com.b2bplatform.operator.model.OperatorReportAccess;
import com.b2bplatform.operator.model.ReportRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OperatorReportAccessRepository extends JpaRepository<OperatorReportAccess, Long> {
    
    Optional<OperatorReportAccess> findByOperatorIdAndUserIdentifier(Long operatorId, String userIdentifier);
    
    List<OperatorReportAccess> findByOperatorIdAndIsActiveTrue(Long operatorId);
    
    List<OperatorReportAccess> findByUserIdentifierAndIsActiveTrue(String userIdentifier);
    
    List<OperatorReportAccess> findByReportRoleAndIsActiveTrue(ReportRole reportRole);
    
    List<OperatorReportAccess> findByUserIdentifierAndReportRoleAndIsActiveTrue(String userIdentifier, ReportRole reportRole);
    
    boolean existsByOperatorIdAndUserIdentifier(Long operatorId, String userIdentifier);
}
