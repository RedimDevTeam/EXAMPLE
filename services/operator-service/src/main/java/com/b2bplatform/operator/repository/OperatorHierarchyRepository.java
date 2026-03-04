package com.b2bplatform.operator.repository;

import com.b2bplatform.operator.model.OperatorHierarchy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OperatorHierarchyRepository extends JpaRepository<OperatorHierarchy, Long> {
    
    Optional<OperatorHierarchy> findByOperatorId(Long operatorId);
    
    List<OperatorHierarchy> findByParentOperatorId(Long parentOperatorId);
    
    List<OperatorHierarchy> findByHierarchyLevel(Integer hierarchyLevel);
    
    List<OperatorHierarchy> findByIsMasterTrue();
    
    List<OperatorHierarchy> findByIsAgentTrue();
    
    List<OperatorHierarchy> findByIsSubAgentTrue();
    
    long countByParentOperatorId(Long parentOperatorId);
    
    boolean existsByOperatorId(Long operatorId);
}
