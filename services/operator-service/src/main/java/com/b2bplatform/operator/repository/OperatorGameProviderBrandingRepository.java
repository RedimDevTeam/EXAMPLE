package com.b2bplatform.operator.repository;

import com.b2bplatform.operator.model.LogoType;
import com.b2bplatform.operator.model.OperatorGameProviderBranding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OperatorGameProviderBrandingRepository extends JpaRepository<OperatorGameProviderBranding, Long> {
    
    List<OperatorGameProviderBranding> findByOperatorIdAndGameProviderIdAndIsActiveTrueOrderByDisplayOrderAsc(
        Long operatorId, String gameProviderId);
    
    List<OperatorGameProviderBranding> findByOperatorIdAndGameProviderIdAndLogoTypeAndIsActiveTrueOrderByDisplayOrderAsc(
        Long operatorId, String gameProviderId, LogoType logoType);
    
    Optional<OperatorGameProviderBranding> findByOperatorIdAndGameProviderIdAndLogoTypeAndDisplayOrder(
        Long operatorId, String gameProviderId, LogoType logoType, Integer displayOrder);
    
    List<OperatorGameProviderBranding> findByOperatorIdOrderByGameProviderIdAscLogoTypeAscDisplayOrderAsc(Long operatorId);
}
