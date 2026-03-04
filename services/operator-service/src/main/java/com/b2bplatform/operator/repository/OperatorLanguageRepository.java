package com.b2bplatform.operator.repository;

import com.b2bplatform.operator.model.OperatorLanguage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OperatorLanguageRepository extends JpaRepository<OperatorLanguage, Long> {
    
    /**
     * Find all languages for an operator.
     */
    List<OperatorLanguage> findByOperatorIdOrderByIsDefaultDescLanguageCodeAsc(Long operatorId);
    
    /**
     * Find active languages for an operator.
     */
    List<OperatorLanguage> findByOperatorIdAndIsActiveTrueOrderByIsDefaultDescLanguageCodeAsc(Long operatorId);
    
    /**
     * Find a specific language for an operator.
     */
    Optional<OperatorLanguage> findByOperatorIdAndLanguageCode(Long operatorId, String languageCode);
    
    /**
     * Find the default language for an operator.
     */
    Optional<OperatorLanguage> findByOperatorIdAndIsDefaultTrue(Long operatorId);
    
    /**
     * Check if operator has a default language.
     */
    @Query("SELECT COUNT(ol) > 0 FROM OperatorLanguage ol WHERE ol.operatorId = :operatorId AND ol.isDefault = true")
    boolean hasDefaultLanguage(@Param("operatorId") Long operatorId);
    
    /**
     * Count active languages for an operator.
     */
    long countByOperatorIdAndIsActiveTrue(Long operatorId);
    
    /**
     * Check if language exists for operator.
     */
    boolean existsByOperatorIdAndLanguageCode(Long operatorId, String languageCode);
}
