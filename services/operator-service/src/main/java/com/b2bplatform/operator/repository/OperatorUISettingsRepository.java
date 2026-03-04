package com.b2bplatform.operator.repository;

import com.b2bplatform.operator.model.OperatorUISettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OperatorUISettingsRepository extends JpaRepository<OperatorUISettings, Long> {
    
    Optional<OperatorUISettings> findByOperatorId(Long operatorId);
    
    boolean existsByOperatorId(Long operatorId);
}
