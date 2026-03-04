package com.b2bplatform.operator.repository;

import com.b2bplatform.operator.model.LogoType;
import com.b2bplatform.operator.model.OperatorGameBranding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OperatorGameBrandingRepository extends JpaRepository<OperatorGameBranding, Long> {
    
    List<OperatorGameBranding> findByOperatorIdAndGameIdAndIsActiveTrueOrderByDisplayOrderAsc(
        Long operatorId, String gameId);
    
    List<OperatorGameBranding> findByOperatorIdAndGameIdAndLogoTypeAndIsActiveTrueOrderByDisplayOrderAsc(
        Long operatorId, String gameId, LogoType logoType);
    
    Optional<OperatorGameBranding> findByOperatorIdAndGameIdAndLogoTypeAndDisplayOrder(
        Long operatorId, String gameId, LogoType logoType, Integer displayOrder);
    
    List<OperatorGameBranding> findByOperatorIdOrderByGameIdAscLogoTypeAscDisplayOrderAsc(Long operatorId);
}
