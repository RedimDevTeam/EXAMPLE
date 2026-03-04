package com.b2bplatform.operator.repository;

import com.b2bplatform.operator.model.LogoType;
import com.b2bplatform.operator.model.OperatorBranding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OperatorBrandingRepository extends JpaRepository<OperatorBranding, Long> {
    
    List<OperatorBranding> findByOperatorIdAndIsActiveTrueOrderByDisplayOrderAsc(Long operatorId);
    
    List<OperatorBranding> findByOperatorIdAndLogoTypeAndIsActiveTrueOrderByDisplayOrderAsc(
        Long operatorId, LogoType logoType);
    
    Optional<OperatorBranding> findByOperatorIdAndLogoTypeAndDisplayOrder(
        Long operatorId, LogoType logoType, Integer displayOrder);
    
    List<OperatorBranding> findByOperatorIdOrderByLogoTypeAscDisplayOrderAsc(Long operatorId);
}
